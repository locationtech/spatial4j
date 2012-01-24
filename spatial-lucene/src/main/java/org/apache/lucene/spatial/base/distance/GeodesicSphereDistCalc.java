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

import static java.lang.Math.toRadians;

/**
 * A base class for a Distance Calculator that assumes a spherical earth model.
 * @author dsmiley
 */
public abstract class GeodesicSphereDistCalc extends AbstractDistanceCalculator {
  protected final double radius;

  public GeodesicSphereDistCalc(double radius) {
    this.radius = radius;
  }

  @Override
  public double distanceToDegrees(double distance) {
    return DistanceUtils.dist2Degrees(distance, radius);
  }

  @Override
  public double degreesToDistance(double degrees) {
    return DistanceUtils.radians2Dist(toRadians(degrees), radius);
  }

  @Override
  public Point pointOnBearingRAD(Point from, double dist, double bearingRAD, SpatialContext ctx) {
    if (dist == 0)
      return from;
    double[] latLon = DistanceUtils.pointOnBearingRAD(
        toRadians(from.getY()), toRadians(from.getX()),
        dist, bearingRAD, null, radius);
    return ctx.makePoint(Math.toDegrees(latLon[1]), Math.toDegrees(latLon[0]));
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    //See http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates Section 3.1, 3.2 and 3.3

    if (distance == 0)
      return from.getBoundingBox();

    double dist_rad = distance / radius;
    double dist_deg = Math.toDegrees(dist_rad);

    if (dist_deg >= 180)//distance is >= opposite side of the globe
      return ctx.getWorldBounds();

    //--calc latitude bounds
    double latN_deg = from.getY() + dist_deg;
    double latS_deg = from.getY() - dist_deg;

    if (latN_deg >= 90 || latS_deg <= -90) {//touches either pole
      //we have special logic for longitude
      double lonW_deg = -180, lonE_deg = 180;//world wrap: 360 deg
      if (latN_deg <= 90 && latS_deg >= -90) {//doesn't pass either pole: 180 deg
        lonW_deg = from.getX()-90;
        lonE_deg = from.getX()+90;
      }
      if (latN_deg > 90)
        latN_deg = 90;
      if (latS_deg < -90)
        latS_deg = -90;

      return ctx.makeRect(lonW_deg, lonE_deg, latS_deg, latN_deg);
    } else {
      //--calc longitude bounds
      double lat_rad = toRadians(from.getY());
      //See URL above for reference. This isn't intuitive.
      double lon_delta_deg = Math.toDegrees(Math.asin( Math.sin(dist_rad) / Math.cos(lat_rad)));

      double lonW_deg = from.getX()-lon_delta_deg;
      double lonE_deg = from.getX()+lon_delta_deg;

      return ctx.makeRect(lonW_deg, lonE_deg, latS_deg, latN_deg);//ctx will normalize longitude
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GeodesicSphereDistCalc that = (GeodesicSphereDistCalc) o;

    if (Double.compare(that.radius, radius) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    long temp = radius != +0.0d ? Double.doubleToLongBits(radius) : 0L;
    return (int) (temp ^ (temp >>> 32));
  }

  @Override
  public final double distance(Point from, double toX, double toY) {
    return distanceLatLonRAD(toRadians(from.getY()), toRadians(from.getX()), toRadians(toY), toRadians(toX)) * radius;
  }

  protected abstract double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2);

  public static class Haversine extends GeodesicSphereDistCalc {

    public Haversine(double radius) {
      super(radius);
    }

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distHaversineRAD(lat1,lon1,lat2,lon2);
    }

  }

  public static class LawOfCosines extends GeodesicSphereDistCalc {

    public LawOfCosines(double radius) {
      super(radius);
    }

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distLawOfCosinesRAD(lat1, lon1, lat2, lon2);
    }

  }

  public static class Vincenty extends GeodesicSphereDistCalc {
    public Vincenty(double radius) {
      super(radius);
    }

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distVincentyRAD(lat1, lon1, lat2, lon2);
    }
  }
}
