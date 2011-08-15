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

package org.apache.lucene.spatial.strategy.vector;

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
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclideanDistanceCalculator;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.*;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.CachingDoubleValueSource;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.strategy.util.ValueSourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TwoDoublesStrategy extends SpatialStrategy<TwoDoublesFieldInfo> {

  static final Logger log = LoggerFactory.getLogger(TwoDoublesStrategy.class);

  private final TrieFieldInfo finfo;
  private final DoubleParser parser;

  public TwoDoublesStrategy(SpatialContext ctx, TrieFieldInfo finfo, DoubleParser parser) {
    super(ctx);
    this.finfo = finfo;
    this.parser = parser;
  }

  @Override
  public boolean isPolyField() {
    return true;
  }

  @Override
  public Fieldable[] createFields(TwoDoublesFieldInfo fieldInfo,
      Shape shape, boolean index, boolean store) {
    if( shape instanceof Point ) {
      Point point = (Point)shape;

      Fieldable[] f = new Fieldable[store?3:2];
      f[0] = finfo.createDouble( fieldInfo.getFieldNameX(), point.getX() );
      f[1] = finfo.createDouble( fieldInfo.getFieldNameY(), point.getY() );
      if( store ) {
        f[2] = new Field( fieldInfo.getFieldName(), ctx.toString( shape ), Store.YES, Index.NO );
      }
      return f;
    }
    if( !ignoreIncompatibleGeometry ) {
      throw new IllegalArgumentException( "TwoDoublesStrategy can not index: "+shape );
    }
    return null;
  }

  @Override
  public Fieldable createField(TwoDoublesFieldInfo indexInfo, Shape shape,
      boolean index, boolean store) {
    throw new UnsupportedOperationException("Point is poly field");
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, TwoDoublesFieldInfo fieldInfo) {
    DistanceCalculator calc = new EuclideanDistanceCalculator();
    return makeValueSource(args, fieldInfo,calc);
  }

  public ValueSource makeValueSource(SpatialArgs args, TwoDoublesFieldInfo fieldInfo, DistanceCalculator calc) {
    Point p = args.getShape().getCenter();
    return new DistanceValueSource(p, calc, fieldInfo, parser);
  }


  @Override
  public Filter makeFilter(SpatialArgs args, TwoDoublesFieldInfo fieldInfo) {
    if( args.getShape() instanceof Circle) {
      if( SpatialOperation.is( args.getOperation(),
          SpatialOperation.Intersects,
          SpatialOperation.IsWithin )) {
        DistanceCalculator calc = ctx.getDistanceCalculator();
        Circle circle = (Circle)args.getShape();
        Query bbox = makeWithin(circle.getBoundingBox(), fieldInfo);

        // Make the ValueSource
        ValueSource valueSource = makeValueSource(args, fieldInfo, calc);

        return new ValueSourceFilter(
            new QueryWrapperFilter( bbox ), valueSource, 0, circle.getDistance() );
      }
    }
    return new QueryWrapperFilter( makeQuery(args, fieldInfo) );
  }

  @Override
  public Query makeQuery(SpatialArgs args, TwoDoublesFieldInfo fieldInfo) {
    // For starters, just limit the bbox
    Shape shape = args.getShape();
    if (!(shape instanceof Rectangle)) {
      throw new InvalidShapeException("A rectangle is the only supported at this time, not "+shape.getClass());//TODO
    }
    Rectangle bbox = (Rectangle) shape;
    if (bbox.getCrossesDateLine()) {
      throw new UnsupportedOperationException( "Crossing dateline not yet supported" );
    }

    ValueSource valueSource = null;
    DistanceCalculator calc = ctx.getDistanceCalculator();

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
      if( args.getShape() instanceof Circle) {
        Circle circle = (Circle)args.getShape();

        // Make the ValueSource
        valueSource = makeValueSource(args, fieldInfo, calc);

        ValueSourceFilter vsf = new ValueSourceFilter(
            new QueryWrapperFilter( spatial ), valueSource, 0, circle.getDistance() );

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
      Query spatialRankingQuery = new FunctionQuery(valueSource);
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
  private Query makeWithin(Rectangle bbox, TwoDoublesFieldInfo fieldInfo) {
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
  Query makeDisjoint(Rectangle bbox, TwoDoublesFieldInfo fieldInfo) {
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




