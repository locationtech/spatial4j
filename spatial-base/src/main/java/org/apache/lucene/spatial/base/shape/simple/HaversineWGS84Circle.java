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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Circle;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * An ellipse-like geometry based on the haversine formula with a supplied earth radius.
 */
public final class HaversineWGS84Circle implements Circle {
  private final Point point;
  private final double distance;
  private final double radius;

  private final Rectangle enclosingBox;//calculated & cached

  public HaversineWGS84Circle(Point p, double dist, double radius, SpatialContext shapeIO) {
    this.point = p;
    this.distance = dist;
    this.radius = radius;
    this.enclosingBox = calcEnclosingBox(shapeIO);
  }

  public Point getCenter() {
    return point;
  }

  @Override
  public double getDistance() {
    return distance;
  }

  public double getRadius() {
    return radius;
  }

  private Rectangle calcEnclosingBox(SpatialContext shapeIO) {
    //!! code copied from LatLonType.createSpatialQuery(); this should be consolidated
    final int LAT = 0;
    final int LONG = 1;

    double[] point = new double[]{this.point.getY(),this.point.getX()};
    point[0] = point[0] * DistanceUtils.DEGREES_TO_RADIANS;
    point[1] = point[1] * DistanceUtils.DEGREES_TO_RADIANS;
    double[] tmp = new double[2];
    //these calculations aren't totally accurate, but it should be good enough
    //TODO: Optimize to do in single calculations.  Would need to deal with poles, prime meridian, etc.
    double [] north = DistanceUtils.pointOnBearing(point[LAT], point[LONG], distance, 0, tmp, radius);
    //This returns the point as radians, but we need degrees b/c that is what the field is stored as
    double ur_lat = north[LAT] * DistanceUtils.RADIANS_TO_DEGREES;//get it now, as we are going to reuse tmp
    double [] east = DistanceUtils.pointOnBearing(point[LAT], point[LONG], distance, DistanceUtils.DEG_90_AS_RADS, tmp, radius);
    double ur_lon = east[LONG] * DistanceUtils.RADIANS_TO_DEGREES;
    double [] south = DistanceUtils.pointOnBearing(point[LAT], point[LONG], distance, DistanceUtils.DEG_180_AS_RADS, tmp, radius);
    double ll_lat = south[LAT] * DistanceUtils.RADIANS_TO_DEGREES;
    double [] west = DistanceUtils.pointOnBearing(point[LAT], point[LONG], distance, DistanceUtils.DEG_270_AS_RADS, tmp, radius);
    double ll_lon = west[LONG] * DistanceUtils.RADIANS_TO_DEGREES;

    //TODO: can we reuse our bearing calculations?
    double angDist = DistanceUtils.angularDistance(distance, radius);//in radians

    // for the poles, do something slightly different - a polar "cap".
    // Also, note point[LAT] is in radians, but ur and ll are in degrees
    if (point[LAT] + angDist > DistanceUtils.DEG_90_AS_RADS) { // we cross the north pole
      ll_lat = Math.min(ll_lat, ur_lat);
      ur_lat = 90;
      ll_lon = -180;
      ur_lon = 180;
    } else if (point[LAT] - angDist < -DistanceUtils.DEG_90_AS_RADS) { // we cross the south pole
      ur_lat = Math.max(ll_lat, ur_lat);
      ll_lat = -90;
      ll_lon = -180;
      ur_lon = 180;
    }

    //(end of code from LatLonType.createSpatialQuery())
    //if (ll_lon <= ur_lon) {
    return shapeIO.makeRect(ll_lon, ur_lon, ll_lat, ur_lat);
//    } else {
//      enclosingBox1 = shapeIO.makeRect(Math.max(ll_lon,ur_lon),180,ll_lat,ur_lat);
//      enclosingBox2 = shapeIO.makeRect(-180,Math.min(ll_lon,ur_lon),ll_lat,ur_lat);
//    }
  }

  public boolean contains(double x, double y) {
    return DistanceUtils.haversine(Math.toRadians(point.getY()), Math.toRadians(point.getX()),
            Math.toRadians(y), Math.toRadians(x), radius) <= distance;
  }

  @Override
  public boolean hasArea() {
    return distance > 0;
  }

  @Override
  public Rectangle getBoundingBox() {
    return enclosingBox;
//    if (enclosingBox2 == null)
//      return enclosingBox1;
//    //wrap longitude around the world (note: both boxes have same latitudes)
//    return new RectangeImpl(-180,180,enclosingBox1.getMinY(),enclosingBox1.getMaxY());
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext context) {
    //do quick check against bounding box for OUTSIDE
    if (enclosingBox.intersect(other,context) == IntersectCase.OUTSIDE) {
//      if (enclosingBox2 == null || enclosingBox2.intersect(other,context) == IntersectCase.OUTSIDE)
      return IntersectCase.OUTSIDE;
    }

    //TODO faster to do this first or here, after enclosingBox ?
    if (other instanceof Point) {
      Point point = (Point) other;
      return contains(point.getX(),point.getY()) ? IntersectCase.CONTAINS : IntersectCase.OUTSIDE;
    }

    //do quick check to see if all corners are within this circle for CONTAINS
    Rectangle bbox = other.getBoundingBox();
    if (contains(bbox.getMinX(),bbox.getMinY()) &&
        contains(bbox.getMinX(),bbox.getMaxY()) &&
        contains(bbox.getMaxX(),bbox.getMaxY()) &&
        contains(bbox.getMaxX(),bbox.getMinY()))
      return IntersectCase.CONTAINS;

    return IntersectCase.INTERSECTS;//needn't actually intersect; this is a good guess
  }

  @Override
  public String toString() {
    return "Circle(" + point + ",dist=" + distance + ')';
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    HaversineWGS84Circle rhs = (HaversineWGS84Circle) obj;
    return new EqualsBuilder()
                  .appendSuper(super.equals(obj))
                  .append(point, rhs.point)
                  .append(distance, rhs.distance)
                  .append(radius, rhs.radius)
                  .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 97).
      append(point).
      append(distance).
      append(radius).
      toHashCode();
  }
}
