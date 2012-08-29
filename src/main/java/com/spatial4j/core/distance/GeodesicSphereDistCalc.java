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

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

import static com.spatial4j.core.distance.DistanceUtils.toDegrees;
import static com.spatial4j.core.distance.DistanceUtils.toRadians;

/**
 * A base class for a Distance Calculator that assumes a spherical earth model.
 */
public abstract class GeodesicSphereDistCalc extends AbstractDistanceCalculator {

  private static final double radiusDEG = DistanceUtils.toDegrees(1);//in degrees

  @Override
  public Point pointOnBearing(Point from, double distDEG, double bearingDEG, SpatialContext ctx, Point reuse) {
    if (distDEG == 0) {
      if (reuse == null)
        return from;
      reuse.reset(from.getX(), from.getY());
      return reuse;
    }
    Point result = DistanceUtils.pointOnBearingRAD(
        toRadians(from.getY()), toRadians(from.getX()),
        toRadians(distDEG),
        toRadians(bearingDEG), ctx, reuse);//output result is in radians
    result.reset(toDegrees(result.getX()), toDegrees(result.getY()));
    return result;
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distDEG, SpatialContext ctx, Rectangle reuse) {
    return DistanceUtils.calcBoxByDistFromPtDEG(from.getY(), from.getX(), distDEG, ctx, reuse);
  }

  @Override
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distDEG, SpatialContext ctx) {
    return DistanceUtils.calcBoxByDistFromPt_latHorizAxisDEG(from.getY(), from.getX(), distDEG);
  }

  @Override
  public double area(Rectangle rect) {
    //From http://mathforum.org/library/drmath/view/63767.html
    double lat1 = toRadians(rect.getMinY());
    double lat2 = toRadians(rect.getMaxY());
    return Math.PI / 180 * radiusDEG * radiusDEG *
            Math.abs(Math.sin(lat1) - Math.sin(lat2)) *
            rect.getWidth();
  }

  @Override
  public double area(Circle circle) {
    //formula is a simplified case of area(rect).
    double lat = toRadians(90 - circle.getRadius());
    return 2 * Math.PI * radiusDEG * radiusDEG * (1 - Math.sin(lat));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    return getClass().equals(obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public final double distance(Point from, double toX, double toY) {
    return toDegrees(distanceLatLonRAD(toRadians(from.getY()), toRadians(from.getX()), toRadians(toY), toRadians(toX)));
  }

  protected abstract double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2);

  public static class Haversine extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distHaversineRAD(lat1,lon1,lat2,lon2);
    }

  }

  public static class LawOfCosines extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distLawOfCosinesRAD(lat1, lon1, lat2, lon2);
    }

  }

  public static class Vincenty extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distVincentyRAD(lat1, lon1, lat2, lon2);
    }
  }
}
