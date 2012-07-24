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

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.*;

/**
 * A circle, also known as a point-radius, based on a {@link
 * com.spatial4j.core.distance.DistanceCalculator} which does all the work. This
 * implementation should work for both cartesian 2D and geodetic sphere
 * surfaces. Threadsafe & immutable.
 */
public class CircleImpl implements Circle {

  protected final Point point;
  protected final double distRadius;

  protected final SpatialContext ctx;

  /* below is calculated & cached: */
  
  protected final Rectangle enclosingBox;

  //we don't have a line shape so we use a rectangle for these axis

  public CircleImpl(Point p, double distRadius, SpatialContext ctx) {
    //We assume any normalization / validation of params already occurred (including bounding dist)
    this.point = p;
    this.distRadius = distRadius;
    this.ctx = ctx;
    this.enclosingBox = ctx.getDistCalc().calcBoxByDistFromPt(point, this.distRadius, ctx);
  }

  @Override
  public Point getCenter() {
    return point;
  }

  @Override
  public double getRadius() {
    return distRadius;
  }

  public boolean contains(double x, double y) {
    return ctx.getDistCalc().distance(point, x, y) <= distRadius;
  }

  @Override
  public boolean hasArea() {
    return distRadius > 0;
  }

  /**
   * Note that the bounding box might contain a minX that is > maxX, due to WGS84 dateline.
   */
  @Override
  public Rectangle getBoundingBox() {
    return enclosingBox;
  }

  @Override
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    assert this.ctx == ctx;
//This shortcut was problematic in testing due to distinctions of CONTAINS/WITHIN for no-area shapes (lines, points).
//    if (distance == 0) {
//      return point.relate(other,ctx).intersects() ? SpatialRelation.WITHIN : SpatialRelation.DISJOINT;
//    }

    if (other instanceof Point) {
      return relate((Point) other, ctx);
    }
    if (other instanceof Rectangle) {
      return relate((Rectangle) other, ctx);
    }
    if (other instanceof Circle) {
      return relate((Circle) other, ctx);
    }
    return other.relate(this, ctx).transpose();
  }

  public SpatialRelation relate(Point point, SpatialContext ctx) {
    return contains(point.getX(),point.getY()) ? SpatialRelation.CONTAINS : SpatialRelation.DISJOINT;
  }

  public SpatialRelation relate(Rectangle r, SpatialContext ctx) {
    //Note: Surprisingly complicated!

    //--We start by leveraging the fact we have a calculated bbox that is "cheaper" than use of DistanceCalculator.
    final SpatialRelation bboxSect = enclosingBox.relate(r, ctx);
    if (bboxSect == SpatialRelation.DISJOINT || bboxSect == SpatialRelation.WITHIN)
      return bboxSect;
    else if (bboxSect == SpatialRelation.CONTAINS && enclosingBox.equals(r))//nasty identity edge-case
      return SpatialRelation.WITHIN;
    //bboxSect is INTERSECTS or CONTAINS
    //The result can be DISJOINT, CONTAINS, or INTERSECTS (not WITHIN)

    return relateRectanglePhase2(r, bboxSect, ctx);
  }

  protected SpatialRelation relateRectanglePhase2(final Rectangle r, SpatialRelation bboxSect, SpatialContext ctx) {
    /*
     !! DOES NOT WORK WITH GEO CROSSING DATELINE OR WORLD-WRAP.
     TODO upgrade to handle crossing dateline, but not world-wrap; use some x-shifting code from RectangleImpl.
     */

    //At this point, the only thing we are certain of is that circle is *NOT* WITHIN r, since the bounding box of a
    // circle MUST be within r for the circle to be within r.

    //--Quickly determine if they are DISJOINT or not.
    //see http://stackoverflow.com/questions/401847/circle-rectangle-collision-detection-intersection/1879223#1879223
    final double closestX;
    double ctr_x = getXAxis();
    if ( ctr_x < r.getMinX() )
      closestX = r.getMinX();
    else if (ctr_x > r.getMaxX())
      closestX = r.getMaxX();
    else
      closestX = ctr_x;

    final double closestY;
    double ctr_y = getYAxis();
    if ( ctr_y < r.getMinY() )
      closestY = r.getMinY();
    else if (ctr_y > r.getMaxY())
      closestY = r.getMaxY();
    else
      closestY = ctr_y;

    //Check if there is an intersection from this circle to closestXY
    boolean didContainOnClosestXY = false;
    if (ctr_x == closestX) {
      double deltaY = Math.abs(ctr_y - closestY);
      double distYCirc = (ctr_y < closestY ? enclosingBox.getMaxY() - ctr_y : ctr_y - enclosingBox.getMinY());
      if (deltaY > distYCirc)
        return SpatialRelation.DISJOINT;
    } else if (ctr_y == closestY) {
      double deltaX = Math.abs(ctr_x - closestX);
      double distXCirc = (ctr_x < closestX ? enclosingBox.getMaxX() - ctr_x : ctr_x - enclosingBox.getMinX());
      if (deltaX > distXCirc)
        return SpatialRelation.DISJOINT;
    } else {
      //fallback on more expensive calculation
      didContainOnClosestXY = true;
      if(! contains(closestX,closestY) )
        return SpatialRelation.DISJOINT;
    }

    //At this point we know that it's *NOT* DISJOINT, so there is some level of intersection. It's *NOT* WITHIN either.
    // The only question left is whether circle CONTAINS r or simply intersects it.

    //If circle contains r, then its bbox MUST also CONTAIN r.
    if (bboxSect != SpatialRelation.CONTAINS)
      return SpatialRelation.INTERSECTS;

    //Find the farthest point of r away from the center of the circle. If that point is contained, then all of r is
    // contained.
    double farthestX = r.getMaxX() - ctr_x > ctr_x - r.getMinX() ? r.getMaxX() : r.getMinX();
    double farthestY = r.getMaxY() - ctr_y > ctr_y - r.getMinY() ? r.getMaxY() : r.getMinY();
    if (contains(farthestX,farthestY))
      return SpatialRelation.CONTAINS;
    return SpatialRelation.INTERSECTS;
  }

  /**
   * The <code>Y</code> coordinate of where the circle axis intersect.
   */
  protected double getYAxis() {
    return point.getY();
  }

  /**
   * The <code>X</code> coordinate of where the circle axis intersect.
   */
  protected double getXAxis() {
    return point.getX();
  }

  public SpatialRelation relate(Circle circle, SpatialContext ctx) {
    double crossDist = ctx.getDistCalc().distance(point, circle.getCenter());
    double aDist = distRadius, bDist = circle.getRadius();
    if (crossDist > aDist + bDist)
      return SpatialRelation.DISJOINT;
    if (crossDist < aDist && crossDist + bDist <= aDist)
      return SpatialRelation.CONTAINS;
    if (crossDist < bDist && crossDist + aDist <= bDist)
      return SpatialRelation.WITHIN;

    return SpatialRelation.INTERSECTS;
  }

  @Override
  public String toString() {
    return "Circle(" + point + ",d=" + distRadius + ')';
  }

  @Override
  public boolean equals(Object obj) {
    return equals(this,obj);
  }

  /**
   * All {@link Circle} implementations should use this definition of {@link Object#equals(Object)}.
   */
  public static boolean equals(Circle thiz, Object o) {
    assert thiz != null;
    if (thiz == o) return true;
    if (!(o instanceof Circle)) return false;

    Circle circle = (Circle) o;

    if (!thiz.getCenter().equals(circle.getCenter())) return false;
    if (Double.compare(circle.getRadius(), thiz.getRadius()) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return hashCode(this);
  }

  /**
   * All {@link Circle} implementations should use this definition of {@link Object#hashCode()}.
   */
  public static int hashCode(Circle thiz) {
    int result;
    long temp;
    result = thiz.getCenter().hashCode();
    temp = thiz.getRadius() != +0.0d ? Double.doubleToLongBits(thiz.getRadius()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
