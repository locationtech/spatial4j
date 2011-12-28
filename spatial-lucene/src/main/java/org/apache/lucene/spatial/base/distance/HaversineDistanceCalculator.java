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

  private final double radius;

  public HaversineDistanceCalculator( double radius ) {
    this.radius = radius;
  }

  @Override
  public double calculate(Point p1, double toX, double toY) {
    return DistanceUtils.haversineRAD(Math.toRadians(p1.getY()), Math.toRadians(p1.getX()),
        Math.toRadians(toY), Math.toRadians(toX), radius);
  }

  @Override
  public double convertDistanceToRadians(double distance) {
    return DistanceUtils.dist2Radians(distance, radius);
  }

  @Override
  public double convertRadiansToDistance(double radius) {
    return DistanceUtils.radians2Dist(radius, this.radius);
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    /*
    This code is very optimized to do the minimum number of calculations
     */

    double angDistance = distance / radius;

    if (angDistance >= Math.PI)//distance is >= opposite side of the globe
      return ctx.getWorldBounds();

    double startLon = Math.toRadians(from.getX());
    double startLat = Math.toRadians(from.getY());

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
      return ctx.makeRect(-180, 180, touchesSouthPole ? -90 : Math.toDegrees(latS) ,90);
    }
    double latN = Math.asin(_a + _b);//reduced form given that cos(0) == +1 (north)
    if (touchesSouthPole) {//but we know it doesn't touch the north pole
      return ctx.makeRect(-180, 180, -90, Math.toDegrees(latN));
    }
    double latS = Math.asin(_a - _b);//reduced form given that cos(PI) == -1 (south)

    double lon_delta = Math.atan2(sinAngDist * cosStartLat, cosAngDist - sinStartLat * sinStartLat);
    double lonW_deg = Math.toDegrees(startLon - lon_delta);
    double lonE_deg = Math.toDegrees(startLon + lon_delta);

    lonW_deg = normLonDEG(lonW_deg);
    lonE_deg = normLonDEG(lonE_deg);
    return ctx.makeRect(lonW_deg, lonE_deg, Math.toDegrees(latS), Math.toDegrees(latN));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HaversineDistanceCalculator that = (HaversineDistanceCalculator) o;

    if (Double.compare(that.radius, radius) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    long temp = radius != +0.0d ? Double.doubleToLongBits(radius) : 0L;
    return (int) (temp ^ (temp >>> 32));
  }
}
