/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.search.point;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.apache.lucene.spatial.search.SpatialStrategy;
import org.apache.lucene.spatial.search.util.TrieFieldHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointStrategy extends SpatialStrategy<PointFieldInfo> {

  static final Logger log = LoggerFactory.getLogger(PointStrategy.class);

  private final TrieFieldHelper.FieldInfo finfo;
  private final DoubleParser parser;
  private final ShapeIO reader;

  public PointStrategy( ShapeIO reader, TrieFieldHelper.FieldInfo finfo, DoubleParser parser ) {
    this.reader = reader;
    this.finfo = finfo;
    this.parser = parser;
  }

  @Override
  public boolean isPolyField() {
    return true;
  }

  @Override
  public Fieldable[] createFields(PointFieldInfo fieldInfo,
      Shape shape, boolean index, boolean store) {
    if( shape instanceof Point ) {
      Point point = (Point)shape;

      Fieldable[] f = new Fieldable[store?3:2];
      f[0] = TrieFieldHelper.createDoubleField(fieldInfo.getFieldNameX(), point.getX(), finfo, 1.0f );
      f[1] = TrieFieldHelper.createDoubleField(fieldInfo.getFieldNameY(), point.getY(), finfo, 1.0f );
      if( store ) {
        f[2] = new Field( fieldInfo.getFieldName(), reader.toString( shape ), Store.YES, Index.NO );
      }
      return f;
    }
    if( !ignoreIncompatibleGeometry ) {
      throw new IllegalArgumentException( "PointStrategy can not index: "+shape );
    }
    return null;
  }

  @Override
  public Fieldable createField(PointFieldInfo indexInfo, Shape shape,
      boolean index, boolean store) {
    throw new UnsupportedOperationException("Point is poly field");
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, PointFieldInfo fieldInfo) {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    if (Point.class.isInstance(args.getShape())) {
      return new DistanceValueSource(((Point)args.getShape()), calc, fieldInfo, parser);
    }
    throw new UnsupportedOperationException( "score only works with point or radius (for now)" );
  }

  @Override
  public Query makeQuery(SpatialArgs args, PointFieldInfo fieldInfo) {
    // For starters, just limit the bbox
    BBox bbox = args.getShape().getBoundingBox();
    if (bbox.getCrossesDateLine()) {
      throw new UnsupportedOperationException( "Crossing dateline not yet supported" );
    }

    Query spatial = null;
    switch (args.getOperation()) {
      case BBoxIntersects:
      case BBoxWithin:
      case Contains:
      case Intersects:
      case IsWithin:
      case Overlaps:
        spatial =  makeWithin(bbox, fieldInfo);
        break;
      case IsDisjointTo:
        spatial =  makeDisjoint(bbox, fieldInfo);
        break;
      case Distance: {
        if (args.getMax() == null && args.getMin() == null) {
          // no bbox to limit
          return new ValueSourceQuery(makeValueSource(args, fieldInfo));
        }
        if (Point.class.isInstance(args.getShape())) {
          // first make a BBox Query
          Point p = (Point) args.getShape();
          double r = args.getMax();
          spatial = makeWithin(new Rectangle(p.getX() - r, p.getX() + r, p.getY() - r, p.getY() + r), fieldInfo);
          break;
        }
        throw new IllegalArgumentException( "Distance only works with point (on point fields)" );
      }

      default:
        throw new UnsupportedOperationException(args.getOperation().name());
    }

    if (args.isCalculateScore()) {
      try {
        Query spatialRankingQuery = new ValueSourceQuery(makeValueSource(args, fieldInfo));
        BooleanQuery bq = new BooleanQuery();
        bq.add(spatial,BooleanClause.Occur.MUST);
        bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
        return bq;
      } catch(Exception ex) {
        log.warn("error making score", ex);
      }
    }
    return spatial;
  }

  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  private Query makeWithin(BBox bbox, PointFieldInfo fieldInfo) {
    Query qX = NumericRangeQuery.newDoubleRange(
      fieldInfo.getFieldNameX(),
      finfo.precisionStep,
      bbox.getMinX(),
      bbox.getMaxX(),
      true,
      true);
    Query qY = NumericRangeQuery.newDoubleRange(
      fieldInfo.getFieldNameY(),
      finfo.precisionStep,
      bbox.getMinY(),
      bbox.getMaxY(),
      true,
      true);

    BooleanQuery bq = new BooleanQuery();
    bq.add(qX,BooleanClause.Occur.MUST);
    bq.add(qY,BooleanClause.Occur.MUST);
    return bq;
  }

  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  Query makeDisjoint(BBox bbox, PointFieldInfo fieldInfo) {
    Query qX = NumericRangeQuery.newDoubleRange(
      fieldInfo.getFieldNameX(),
      finfo.precisionStep,
      bbox.getMinX(),
      bbox.getMaxX(),
      true,
      true);
    Query qY = NumericRangeQuery.newDoubleRange(
      fieldInfo.getFieldNameY(),
      finfo.precisionStep,
      bbox.getMinY(),
      bbox.getMaxY(),
      true,
      true);

    BooleanQuery bq = new BooleanQuery();
    bq.add(qX,BooleanClause.Occur.MUST_NOT);
    bq.add(qY,BooleanClause.Occur.MUST_NOT);
    return bq;
  }
}




