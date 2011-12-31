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
  public double distanceToDegrees(double distance) {
    return DistanceUtils.dist2Degrees(distance, radius);
  }

  @Override
  public double degreesToDistance(double radius) {
    return DistanceUtils.radians2Dist(Math.toRadians(radius), this.radius);
  }

  @Override
  public Point pointOnBearingRAD(Point from, double dist, double bearingRAD, SpatialContext ctx) {
    if (dist == 0)
      return from;
    double[] latLon = DistanceUtils.pointOnBearingRAD(
        Math.toRadians(from.getY()), Math.toRadians(from.getX()),
        dist, bearingRAD, null, radius);
    return ctx.makePoint(Math.toDegrees(latLon[1]),Math.toDegrees(latLon[0]));
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    /*
    This code is very optimized to do the minimum number of calculations
     */
    if (distance == 0)
      return from.getBoundingBox();

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

    double northernOverlap = startLat + angDistance - DEG_90_AS_RADS;
    double southernOverlap = -DEG_90_AS_RADS - (startLat - angDistance);
    if (northernOverlap >= 0 || southernOverlap >= 0) {//touches either pole
      double lonW_deg = -180, lonE_deg = 180;//world wrap: 360 deg
      if (northernOverlap <= 0 && southernOverlap <= 0) {//doesn't pass either pole: 180 deg
        lonW_deg = from.getX()-90;
        lonE_deg = from.getX()+90;
      }
      double latS_deg = -90, latN_deg = 90;
      if (northernOverlap < 0) {//doesn't touch north pole
        latN_deg = Math.toDegrees(Math.asin(_a + _b));//reduced form given that cos(0) == +1 (north)
      }
      if (southernOverlap < 0) {//doesn't touch south pole
        latS_deg = Math.toDegrees(Math.asin(_a - _b));//reduced form given that cos(PI) == -1 (south)
      }
      return ctx.makeRect(lonW_deg, lonE_deg, latS_deg, latN_deg);
    }

    double lon_delta = Math.atan2(sinAngDist * cosStartLat, cosAngDist - sinStartLat * sinStartLat);
    double lonW_deg = Math.toDegrees(startLon - lon_delta);
    double lonE_deg = Math.toDegrees(startLon + lon_delta);

    lonW_deg = normLonDEG(lonW_deg);
    lonE_deg = normLonDEG(lonE_deg);

    double latN_deg = Math.toDegrees(Math.asin(_a + _b));//reduced form given that cos(0) == +1 (north)
    double latS_deg = Math.toDegrees(Math.asin(_a - _b));//reduced form given that cos(PI) == -1 (south)

    return ctx.makeRect(lonW_deg, lonE_deg, latS_deg, latN_deg);
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
