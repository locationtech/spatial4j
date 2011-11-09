/*
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

package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;

import static org.apache.lucene.spatial.base.distance.DistanceUtils.*;

public class HaversineDistanceCalculator extends AbstractDistanceCalculator {

  final double radius;


  public HaversineDistanceCalculator( double radius ) {
    this.radius = radius;
  }

  @Override
  public double calculate(Point p1, double toX, double toY) {
    return DistanceUtils.haversine( Math.toRadians(p1.getX()), Math.toRadians(p1.getY()),
        Math.toRadians(toX), Math.toRadians(toY), radius );
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    /*
    This code is very optimized to do the minimum number of calculations
     */

    double angDistance = distance / radius;

    if (angDistance >= Math.PI)//distance is >= opposite side of the globe
      return ctx.getWorldBounds();

    double startLon = from.getX() * DEGREES_TO_RADIANS;
    double startLat = from.getY() * DEGREES_TO_RADIANS;

    double sinStartLat = Math.sin(startLat);
    double cosStartLat = Math.cos(startLat);
    double cosAngDist = Math.cos(angDistance);
    double sinAngDist = Math.sin(angDistance);

    final double _a = sinStartLat * cosAngDist;
    final double _b = cosStartLat * sinAngDist;

    boolean touchesNorthPole = startLat + angDistance >= DEG_90_AS_RADS;
    boolean touchesSouthPole = startLat - angDistance <= -DEG_90_AS_RADS;

    if (touchesNorthPole) {
      double latS = Math.asin(_a - _b);//reduced form given that cos(PI) == -1 (south)
      return ctx.makeRect(-180, 180, touchesSouthPole ? -90 : latS*RADIANS_TO_DEGREES ,90);
    }
    double latN = Math.asin(_a + _b);//reduced form given that cos(0) == +1 (north)
    if (touchesSouthPole) {//but we know it doesn't touch the north pole
      return ctx.makeRect(-180, 180, -90, latN*RADIANS_TO_DEGREES);
    }
    double latS = Math.asin(_a - _b);//reduced form given that cos(PI) == -1 (south)

    double lon_delta = Math.atan2(sinAngDist * cosStartLat, cosAngDist - sinStartLat * sinStartLat);
    double lonW_deg = (startLon - lon_delta)*RADIANS_TO_DEGREES;
    double lonE_deg = (startLon + lon_delta)*RADIANS_TO_DEGREES;

    lonW_deg = normLonDeg(lonW_deg);
    lonE_deg = normLonDeg(lonE_deg);
    return ctx.makeRect(lonW_deg, lonE_deg, latS*RADIANS_TO_DEGREES, latN*RADIANS_TO_DEGREES);
  }

}
