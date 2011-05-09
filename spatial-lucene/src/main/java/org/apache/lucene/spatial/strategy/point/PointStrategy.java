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

package org.apache.lucene.spatial.strategy.point;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.PointDistanceShape;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.CachingDoubleValueSource;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.strategy.util.ValueSourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointStrategy extends SpatialStrategy<PointFieldInfo> {

  static final Logger log = LoggerFactory.getLogger(PointStrategy.class);

  private final TrieFieldInfo finfo;
  private final DoubleParser parser;
  private final SpatialContext reader;

  public PointStrategy( SpatialContext reader, TrieFieldInfo finfo, DoubleParser parser ) {
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
      f[0] = finfo.createDouble( fieldInfo.getFieldNameX(), point.getX() );
      f[1] = finfo.createDouble( fieldInfo.getFieldNameY(), point.getY() );
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
    return makeValueSource(args, fieldInfo,calc);
  }

  public ValueSource makeValueSource(SpatialArgs args, PointFieldInfo fieldInfo, DistanceCalculator calc) {
    Point p = args.getShape().getCenter();
    return new DistanceValueSource(p, calc, fieldInfo, parser);
  }


  @Override
  public Filter makeFilter(SpatialArgs args, PointFieldInfo fieldInfo) {
    if( args.getShape() instanceof PointDistanceShape ) {
      if( SpatialOperation.is( args.getOperation(),
          SpatialOperation.Intersects,
          SpatialOperation.IsWithin )) {
        DistanceCalculator calc = reader.getDistanceCalculator();
        PointDistanceShape pd = (PointDistanceShape)args.getShape();
        Query bbox = makeWithin(pd.getBoundingBox(), fieldInfo);

        // Make the ValueSource
        ValueSource valueSource = makeValueSource(args, fieldInfo, calc);

        return new ValueSourceFilter(
            new QueryWrapperFilter( bbox ), valueSource, 0, pd.getDistance() );
      }
    }
    return new QueryWrapperFilter( makeQuery(args, fieldInfo) );
  }

  @Override
  public Query makeQuery(SpatialArgs args, PointFieldInfo fieldInfo) {
    // For starters, just limit the bbox
    BBox bbox = args.getShape().getBoundingBox();
    if (bbox.getCrossesDateLine()) {
      throw new UnsupportedOperationException( "Crossing dateline not yet supported" );
    }

    ValueSource valueSource = null;
    DistanceCalculator calc = reader.getDistanceCalculator();

    Query spatial = null;
    SpatialOperation op = args.getOperation();

    if( SpatialOperation.is( op,
        SpatialOperation.BBoxWithin,
        SpatialOperation.BBoxIntersects ) ) {
        spatial = makeWithin(bbox, fieldInfo);
    }
    else if( SpatialOperation.is( op,
      SpatialOperation.Intersects,
      SpatialOperation.IsWithin ) ) {
      spatial = makeWithin(bbox, fieldInfo);
      if( args.getShape() instanceof PointDistanceShape ) {
        PointDistanceShape pd = (PointDistanceShape)args.getShape();

        // Make the ValueSource
        valueSource = makeValueSource(args, fieldInfo, calc);

        ValueSourceFilter vsf = new ValueSourceFilter(
            new QueryWrapperFilter( spatial ), valueSource, 0, pd.getDistance() );

        spatial = new FilteredQuery( new MatchAllDocsQuery(), vsf );
      }
    }
    else if( op == SpatialOperation.IsDisjointTo ) {
      spatial =  makeDisjoint(bbox, fieldInfo);
    }

    if( spatial == null ) {
      throw new UnsupportedSpatialOperation(args.getOperation());
    }

    try {
      if( valueSource != null ) {
        valueSource = new CachingDoubleValueSource(valueSource);
      }
      else {
        valueSource = makeValueSource(args, fieldInfo, calc);
      }
      Query spatialRankingQuery = new ValueSourceQuery(valueSource);
      BooleanQuery bq = new BooleanQuery();
      bq.add(spatial,BooleanClause.Occur.MUST);
      bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
      return bq;
    } catch(Exception ex) {
      log.warn("error making score", ex);
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




