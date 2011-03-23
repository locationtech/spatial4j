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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Point;
import org.apache.lucene.spatial.base.Radius;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointQueryBuilder
{
  static final Logger log = LoggerFactory.getLogger( PointQueryBuilder.class );

  public ValueSource makeValueSource(PointFieldInfo fields, SpatialArgs args)
  {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    if( args.shape instanceof Radius ) {
      DistanceValueSource vs = new DistanceValueSource( ((Radius)args.shape).getPoint(),
          calc, fields );
      vs.max = ((Radius)args.shape).getRadius();
      return vs;
    }
    if( args.shape instanceof Point ) {
      return new DistanceValueSource( ((Point)args.shape), calc, fields );
    }
    throw new UnsupportedOperationException( "score only works with point or radius (for now)" );
  }

  public Query makeQuery(PointFieldInfo fields, SpatialArgs args)
  {
    // For starters, just limit the bbox
    BBox bbox = args.shape.getBoundingBox();
    if( bbox.getCrossesDateLine() ) {
      throw new UnsupportedOperationException( "Crossing dateline not yet supported" );
    }

    PointQueryHelper helper = new PointQueryHelper(bbox,fields);
    Query spatial = null;
    switch( args.op )
    {
      case BBoxIntersects: spatial = helper.makeWithin(); break;
      case BBoxWithin: spatial =  helper.makeWithin(); break;
      case Contains: spatial =  helper.makeWithin(); break;
      case Intersects: spatial =  helper.makeWithin(); break;
      case IsWithin: spatial =  helper.makeWithin(); break;
      case Overlaps: spatial =  helper.makeWithin(); break;
//      case IsEqualTo: spatial =  helper.makeEquals(); break;
//      case IsDisjointTo: spatial =  helper.makeDisjoint(); break;
      default:
        throw new UnsupportedOperationException( args.op.name() );
    }

    if( args.calculateScore ) {
      try {
        Query spatialRankingQuery = new ValueSourceQuery( makeValueSource( fields, args ) );
        BooleanQuery bq = new BooleanQuery();
        bq.add(spatial,BooleanClause.Occur.MUST);
        bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
        return bq;
      }
      catch( Exception ex ) {
        log.warn( "error making score", ex );
      }
    }
    return spatial;
  }
}


class PointQueryHelper
{
  final BBox queryExtent;
  final PointFieldInfo field;

  public PointQueryHelper( BBox bbox, PointFieldInfo field )
  {
    this.queryExtent = bbox;
    this.field = field;
  }

  //-------------------------------------------------------------------------------
  //
  //-------------------------------------------------------------------------------

  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  Query makeWithin()
  {
    Query qX = NumericRangeQuery.newDoubleRange(field.fieldX,field.precisionStep,queryExtent.getMinX(),queryExtent.getMaxX(),true,true);
    Query qY = NumericRangeQuery.newDoubleRange(field.fieldY,field.precisionStep,queryExtent.getMinY(),queryExtent.getMaxY(),true,true);

    BooleanQuery bq = new BooleanQuery();
    bq.add(qX,BooleanClause.Occur.MUST);
    bq.add(qY,BooleanClause.Occur.MUST);
    return bq;
  }
}




