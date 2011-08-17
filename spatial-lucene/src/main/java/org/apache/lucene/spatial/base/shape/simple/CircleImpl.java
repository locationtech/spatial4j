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
import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.*;

/**
 * A circle, otherwise known as a point-radius, which is based on a
 * {@link org.apache.lucene.spatial.base.distance.DistanceCalculator} which does all the work. This implementation
 * should work for both Euclidean 2D and Haversine/WGS84 surfaces.
 * Threadsafe & immutable.
 */
public final class CircleImpl implements Circle {
  private final Point point;
  private final double distance;

  private final SpatialContext ctx;//TODO store the distance-calculator instead?
  
  private final Rectangle enclosingBox;//calculated & cached

  public CircleImpl(Point p, double dist, SpatialContext ctx) {
    if (!ctx.isGeo())
      throw new IllegalArgumentException("Expecting geo SpatialContext but didn't get one: "+ctx);
    this.point = p;
    this.distance = dist;
    this.ctx = ctx;
    this.enclosingBox = calcEnclosingBox(ctx);
  }

  public Point getCenter() {
    return point;
  }

  @Override
  public double getDistance() {
    return distance;
  }

  private Rectangle calcEnclosingBox(SpatialContext ctx) {
    assert this.ctx == ctx;
    return ctx.getDistanceCalculator().calcBoxByDistFromPt(getCenter(), distance, ctx);
  }

  public boolean contains(double x, double y) {
    return ctx.getDistanceCalculator().calculate(point, x, y) <= distance;
  }

  @Override
  public boolean hasArea() {
    return distance > 0;
  }

  /**
   * Note that the bounding box might contain a minX that is > maxX, due to WGS84 dateline.
   * @return
   */
  @Override
  public Rectangle getBoundingBox() {
    return enclosingBox;
//    if (enclosingBox2 == null)
//      return enclosingBox1;
//    //wrap longitude around the world (note: both boxes have same latitudes)
//    return new RectangleImpl(-180,180,enclosingBox1.getMinY(),enclosingBox1.getMaxY());
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext context) {
    assert ctx == context;
    if (other instanceof Point) {
      Point point = (Point) other;
      return contains(point.getX(),point.getY()) ? IntersectCase.CONTAINS : IntersectCase.OUTSIDE;
    }

    if (other instanceof Rectangle) {
      //TODO DWS: update this algorithm to be much faster
      //do quick check against bounding box for OUTSIDE
      if (enclosingBox.intersect(other,context) == IntersectCase.OUTSIDE) {
//      if (enclosingBox2 == null || enclosingBox2.intersect(other,context) == IntersectCase.OUTSIDE)
        return IntersectCase.OUTSIDE;
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

    if (other instanceof Circle) {
      Circle circle = (Circle)other;
      double crossDist = ctx.getDistanceCalculator().calculate(point, circle.getCenter());
      double aDist = distance, bDist = circle.getDistance();
      if (crossDist > aDist + bDist)
        return IntersectCase.OUTSIDE;

      if (crossDist < aDist && crossDist + bDist <= aDist)
        return IntersectCase.CONTAINS;
      if (crossDist < bDist && crossDist + aDist <= bDist)
        return IntersectCase.WITHIN;

      return IntersectCase.INTERSECTS;
    }

    return other.intersect(this, context).transpose();

  }

  @Override
  public String toString() {
    //I'm deliberately making this look basic and not fully detailed with class name & misc fields.
    return "Circle(" + point + ",d=" + distance + ')';
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    CircleImpl rhs = (CircleImpl) obj;
    return new EqualsBuilder()
                  .appendSuper(super.equals(obj))
                  .append(point, rhs.point)
                  .append(distance, rhs.distance)
                  .append(ctx, rhs.ctx)
                  .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 97).
      append(point).
      append(distance).
      append(ctx).
      toHashCode();
  }
}
