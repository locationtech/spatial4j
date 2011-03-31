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

package org.apache.lucene.spatial.base.context;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.PointDistanceShape;
import org.apache.lucene.spatial.base.shape.Shape;

public abstract class AbstractSpatialContext extends SpatialContext {

  protected DistanceUnits units;

  public AbstractSpatialContext( DistanceUnits units )
  {
    this.units = units;
  }

  public Shape readStandardShape(String str) {
    if (str.length() < 1) {
      throw new InvalidShapeException(str);
    }

    if(Character.isLetter(str.charAt(0))) {
      if( str.startsWith( "PointDistance(" ) ) {
        int idx = str.lastIndexOf( ')' );
        if( idx > 0 ) {
          String body = str.substring( "PointDistance(".length(), idx );
          StringTokenizer st = new StringTokenizer(body, " ");
          double x = Double.parseDouble(st.nextToken());
          double y = Double.parseDouble(st.nextToken());
          Double d = null;

          String arg = st.nextToken();
          idx = arg.indexOf( '=' );
          if( idx > 0 ) {
            String k = arg.substring( 0,idx );
            if( k.equals( "d" ) || k.equals( "distance" ) ) {
              d = Double.parseDouble( arg.substring(idx+1));
            }
            else {
              throw new InvalidShapeException( "unknown arg: "+k+" :: " +str );
            }
          }
          else {
            d = Double.parseDouble(arg);
          }
          if( st.hasMoreTokens() ) {
            throw new InvalidShapeException( "Extra arguments: "+st.nextToken()+" :: " +str );
          }
          if( d == null ) {
            throw new InvalidShapeException( "Missing Distance: "+str );
          }
          Point p = makePoint( x, y );
          return new PointDistanceShape( p, d, units.earthRadius(), this );
        }
      }
      return null;
    }

    StringTokenizer st = new StringTokenizer(str, " ");
    double p0 = Double.parseDouble(st.nextToken());
    double p1 = Double.parseDouble(st.nextToken());
    if (st.hasMoreTokens()) {
      double p2 = Double.parseDouble(st.nextToken());
      double p3 = Double.parseDouble(st.nextToken());
      return makeBBox(p0, p2, p1, p3);
    }
    return makePoint(p0, p1);
  }

  public DistanceUnits getUnits() {
    return units;
  }

  public String writeBBox(BBox bbox) {
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(6);
    nf.setMinimumFractionDigits(6);

    return
      nf.format(bbox.getMinX()) + " " +
      nf.format(bbox.getMinY()) + " " +
      nf.format(bbox.getMaxX()) + " " +
      nf.format(bbox.getMaxY());
  }

  @Override
  public DistanceCalculator getDistanceCalculator( Class<? extends DistanceCalculator> clazz )
  {
    if( clazz == null ) {
      return new EuclidianDistanceCalculator();
    }
    return null;
  }
}
