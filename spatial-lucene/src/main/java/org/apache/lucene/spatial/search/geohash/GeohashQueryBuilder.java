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

package org.apache.lucene.spatial.search.geohash;

import java.util.Arrays;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.distance.MultiGeom;
import org.apache.lucene.spatial.base.distance.PointDistanceGeom;
import org.apache.lucene.spatial.base.exception.InvalidSpatialArgument;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;


public class GeohashQueryBuilder implements SpatialQueryBuilder<SimpleSpatialFieldInfo> {

  private final GridReferenceSystem gridReferenceSystem;
  
  public GeohashQueryBuilder( GridReferenceSystem gridReferenceSystem ) {
    this.gridReferenceSystem = gridReferenceSystem;
  }
  
  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException( "score only works with point or radius (for now)" );
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    if(!( args.getOperation() == SpatialOperation.IsWithin ) ||
        ( args.getOperation() == SpatialOperation.BBoxWithin )){
      throw new UnsupportedOperationException(args.getOperation().name());
    }

    Shape qshape = args.getShape();
    if (Point.class.isInstance(args.getShape())) {
      Double dist = args.getDistance();
      Double radius = args.getRadius();
      if( dist == null || radius == null ) {
        throw new InvalidSpatialArgument( "geohash point query needs distance & radius arguments" );
      }

      Point p = (Point)args.getShape();
      PointDistanceGeom pDistGeo = new PointDistanceGeom(p,dist,radius);

      if (args.getOperation() == SpatialOperation.BBoxWithin) {
        qshape = pDistGeo.getEnclosingBox1();
        Shape shape2 = pDistGeo.getEnclosingBox2();
        if (shape2 != null)
          qshape = new MultiGeom(Arrays.asList(qshape,shape2));
      }
    }
    return new ConstantScoreQuery(new GeoHashPrefixFilter(
        fieldInfo.getFieldName(),gridReferenceSystem,qshape));
  }
}




