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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.*;

/**
 * A circle, also known as a point-radius, based on a
 * {@link org.apache.lucene.spatial.base.distance.DistanceCalculator} which does all the work. This implementation
 * should work for both Euclidean 2D and Haversine/WGS84 surfaces.
 * Threadsafe & immutable.
 */
public class CircleImpl implements Circle {
  private final Point point;
  private final double distance;

  private final SpatialContext ctx;

  /* below is calculated & cached: */
  
  private final Rectangle enclosingBox;

  private final CircleImpl inverseCircle;//when distance reaches > 1/2 way around the world, cache the inverse.

  //we don't have a line shape so we use a rectangle for these axis

  //note: It is be possible to instead store one double (y_distDEG instead of these and then calculate the necessary
  // intersections when needed; use intersectXRange(), intersectYRange()

  private final Rectangle x_axis;//can wrap; don't need -back variants
  private final Rectangle y_frontalAxis;//rectangles can't wrap a pole so we need the below two:
  private final Rectangle y_backNorthAxis;//null if none
  private final Rectangle y_backSouthAxis;//null if none

  public CircleImpl(Point p, double dist, SpatialContext ctx) {
    //We assume any normalization / validation of params already occurred (including bounding dist)
    this.point = p;
    this.distance = dist;
    this.ctx = ctx;
    this.enclosingBox = calcEnclosingBox(ctx);

    x_axis = ctx.makeRect(enclosingBox.getMinX(), enclosingBox.getMaxX(),
        this.getCenter().getY(), this.getCenter().getY());

    y_frontalAxis = ctx.makeRect(this.getCenter().getX(), this.getCenter().getX(),
        enclosingBox.getMinY(), enclosingBox.getMaxY());

    //check for possibility of back north or south axis when geo
    if (ctx.isGeo() && dist > 0) {
      //In the direction of latitude (N,S), distance is the same number of degrees.
      double distDEG = ctx.getDistanceCalculator().distanceToDegrees(distance);
      double backX = point.getX() + 180;
      if (enclosingBox.getMaxY() == 90) {
        double diffY = Math.abs(distDEG + point.getY() - 90);//abs() only needed due to numeric imprecision
        y_backNorthAxis = ctx.makeRect(backX,backX,90-diffY,90);
      } else
        y_backNorthAxis = null;
      if (enclosingBox.getMinY() == 90) {
        double diffY = Math.abs(-90 - (point.getY() - distDEG));//abs() only needed due to numeric imprecision
        y_backSouthAxis = ctx.makeRect(backX,backX,-90,-90+diffY);
      } else {
        y_backSouthAxis = null;
      }
      
      if (distDEG > 90) {
        double backDistance = ctx.getDistanceCalculator().degreesToDistance(180 - distDEG);
        inverseCircle = new CircleImpl(ctx.makePoint(point.getX()+180,point.getY()+180),backDistance,ctx);
      } else {
        inverseCircle = null;
      }
    } else {
      y_backNorthAxis = null;
      y_backSouthAxis = null;
      inverseCircle = null;
    }

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
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext ctx) {
    assert this.ctx == ctx;
    if (other instanceof Point) {
      Point point = (Point) other;
      return contains(point.getX(),point.getY()) ? IntersectCase.CONTAINS : IntersectCase.OUTSIDE;
    }

    if (other instanceof Rectangle) {
      final Rectangle r = (Rectangle) other;
      //Note: Surprisingly complicated!

      //--We start by leveraging the fact we have a calculated bbox that is "cheaper" than use of DistanceCalculator.
      final IntersectCase bboxSect = enclosingBox.intersect(r,ctx);
      if (bboxSect == IntersectCase.OUTSIDE || bboxSect == IntersectCase.WITHIN)
        return bboxSect;
      else if (bboxSect == IntersectCase.CONTAINS && enclosingBox.equals(r))//nasty identity edge-case
        return IntersectCase.WITHIN;
      //bboxSect is INTERSECTS or CONTAINS

      if (ctx.isGeo() && (enclosingBox.getCrossesDateLine() || r.getCrossesDateLine()
          || enclosingBox.getWidth()==360 || r.getWidth()==360)) {
        return intersectRectangleGeoCapable(r, bboxSect, ctx);
      } else
        return intersect2DRectangle(r, bboxSect, ctx);
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

    return other.intersect(this, ctx).transpose();
  }

  /** Handles geospatial contexts that involve world wrap &/ pole wrap (and non-geo too). */
  private IntersectCase intersectRectangleGeoCapable(Rectangle r, IntersectCase bboxSect, SpatialContext ctx) {

    if (inverseCircle != null) {
      return inverseCircle.intersect(r,ctx).inverse();
    }

    if (ctx.isGeo() && r.getWidth() == 360) {
      //Rectangle wraps around the world longitudinally.  There are no corners to test.
      //TODO
//      if (r.intersect(y_frontalAxis,ctx).intersects()
//          || (y_backNorthAxis != null && r.intersect(y_backNorthAxis,ctx).intersects())
//          || (y_backSouthAxis != null && r.intersect(y_backSouthAxis,ctx).intersects()))
//        return IntersectCase.INTERSECTS;
//
//      return IntersectCase.OUTSIDE;
    }

    //do quick check to see if all corners are within this circle for CONTAINS
    int cornersIntersect = numCornersIntersect(r);
    if (cornersIntersect == 4) {
      //check for reverse wrap-around
      IntersectCase xIntersect = r.intersect_xRange(x_axis.getMinX(),x_axis.getMaxX(),ctx);
      if (xIntersect == IntersectCase.CONTAINS)
        return IntersectCase.CONTAINS;
      return IntersectCase.INTERSECTS;
    }

    //INTERSECT or OUTSIDE ?
    if (cornersIntersect > 0)
      return IntersectCase.INTERSECTS;
    
    //Now we check if one of the axis of the circle intersect with r.  If so we have
    // intersection
    //TODO make more efficient
    if (r.intersect(x_axis,ctx).intersects()
        || r.intersect(y_frontalAxis,ctx).intersects()
        || (y_backNorthAxis != null && r.intersect(y_backNorthAxis,ctx).intersects())
        || (y_backSouthAxis != null && r.intersect(y_backSouthAxis,ctx).intersects()))
      return IntersectCase.INTERSECTS;

    return IntersectCase.OUTSIDE;
  }

  /** Returns either 0 for none, 1 for some, or 4 for all. */
  private int numCornersIntersect(Rectangle r) {
    //We play some logic games to avoid calling contains() which can be expensive.
    boolean bool;//if true then all corners intersect, if false then no corners intersect
    // for partial, we exit early with 1 and ignore bool.
    bool = (contains(r.getMinX(),r.getMinY()));
    if (contains(r.getMinX(),r.getMaxY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    if (contains(r.getMaxX(),r.getMinY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    if (contains(r.getMaxX(),r.getMaxY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    return bool?4:0;
  }

  /** !! DOES NOT WORK WITH CROSSING DATELINE OR WORLD-WRAP.
   * TODO upgrade to handle crossing dateline, but not world-wrap; use some x-shifting code from RectangleImpl. */
  private IntersectCase intersect2DRectangle(final Rectangle r, IntersectCase bboxSect, SpatialContext ctx) {
    //At this point, the only thing we are certain of is that circle is *NOT* WITHIN r, since the bounding box of a
    // circle MUST be within r for the circle to be within r.

    //--Quickly determine if they are OUTSIDE or not.
    //see http://stackoverflow.com/questions/401847/circle-rectangle-collision-detection-intersection/1879223#1879223
    final double closestX;
    if ( point.getX() < r.getMinX() )
      closestX = r.getMinX();
    else if (point.getX() > r.getMaxX())
      closestX = r.getMaxX();
    else
      closestX = point.getX();

    final double closestY;
    if ( point.getY() < r.getMinY() )
      closestY = r.getMinY();
    else if (point.getY() > r.getMaxY())
      closestY = r.getMaxY();
    else
      closestY = point.getY();

    //Check if there is an intersection from this circle to closestXY
    boolean didContainOnClosestXY = false;
    if (point.getX() == closestX) {
      double distY = Math.abs(point.getY() - closestY);
      double distanceYDEG = enclosingBox.getHeight()/2;//for non-geo, this is equal to distance
      if (distY > distanceYDEG)
        return IntersectCase.OUTSIDE;
    } else if (point.getY() == closestY) {
      double distX = Math.abs(point.getX() - closestX);
      double distanceXDEG = enclosingBox.getWidth()/2;//for non-geo, this is equal to distance
      if (distX > distanceXDEG)
        return IntersectCase.OUTSIDE;
    } else {
      //fallback on more expensive DistanceCalculator
      didContainOnClosestXY = true;
      if(! contains(closestX,closestY) )
        return IntersectCase.OUTSIDE;
    }

    //At this point we know that it's *NOT* OUTSIDE, so there is some level of intersection. It's *NOT* WITHIN either.
    // The only question left is whether circle CONTAINS r or simply intersects it.

    //if circle contains r, then its bbox MUST also CONTAIN r.
    if (bboxSect != IntersectCase.CONTAINS)
      return IntersectCase.INTERSECTS;

    //Find the furthest point of r away from the center of the circle. If that point is contained, then all of r is
    // contained.
    double farthestX = r.getMaxX() - point.getX() > point.getX() - r.getMinX() ? r.getMaxX() : r.getMinX();
    double farthestY = r.getMaxY() - point.getY() > point.getY() - r.getMinY() ? r.getMaxY() : r.getMinY();
    if (contains(farthestX,farthestY))
      return IntersectCase.CONTAINS;
    return IntersectCase.INTERSECTS;

    //--check if all corners of r are within the circle. We have to use DistanceCalculator.
//TODO what's up with this commented code?
//    for (int i = 0; i < 4; i++) {
//      double iX = (i == 0 || i == 1) ? r.getMinX() : r.getMaxX();
//      double iY = (i == 0 || i == 2) ? r.getMinY() : r.getMaxY();
//      if (didContainOnClosestXY && iX == closestX && iY == closestY)
//        continue;//we already know this pair of x & y is contained.
//      if (! contains(iX,iY) ) {
//        return IntersectCase.INTERSECTS;//some corners contain, some don't
//      }
//    }
//    return IntersectCase.CONTAINS;
  }

  @Override
  public String toString() {
    //I'm deliberately making this look basic and not fully detailed with class name & misc fields.
    //Add distance in degrees, which is easier to recognize and earth radius agnostic
    String dStr = String.format("%.1f",distance);
    if (ctx.isGeo()) {
      double distDEG = ctx.getDistanceCalculator().distanceToDegrees(distance);
      dStr += String.format("=%.1f\u00B0",distDEG);
    }
    return "Circle(" + point + ",d=" + dStr + ')';
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
