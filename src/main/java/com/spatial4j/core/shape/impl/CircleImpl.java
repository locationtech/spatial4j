/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

/**
 * A circle, also known as a point-radius, based on a {@link
 * com.spatial4j.core.distance.DistanceCalculator} which does all the work. This
 * implementation should work for both cartesian 2D and geodetic sphere
 * surfaces.
 */
public class CircleImpl implements Circle {

  protected final SpatialContext ctx;

  protected final Point point;
  protected double radiusDEG;

  // calculated & cached
  protected Rectangle enclosingBox;

  public CircleImpl(Point p, double radiusDEG, SpatialContext ctx) {
    //We assume any validation of params already occurred (including bounding dist)
    this.ctx = ctx;
    this.point = p;
    this.radiusDEG = point.isEmpty() ? Double.NaN : radiusDEG;
    this.enclosingBox = point.isEmpty() ? ctx.makeRectangle(Double.NaN, Double.NaN, Double.NaN, Double.NaN) :
      ctx.getDistCalc().calcBoxByDistFromPt(point, this.radiusDEG, ctx, null);
  }

  @Override
  public void reset(double x, double y, double radiusDEG) {
    assert ! isEmpty();
    point.reset(x, y);
    this.radiusDEG = radiusDEG;
    this.enclosingBox = ctx.getDistCalc().calcBoxByDistFromPt(point, this.radiusDEG, ctx, enclosingBox);
  }

  @Override
  public boolean isEmpty() {
    return point.isEmpty();
  }

  @Override
  public Point getCenter() {
    return point;
  }

  @Override
  public double getRadius() {
    return radiusDEG;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    if (ctx == null) {
      return Math.PI * radiusDEG * radiusDEG;
    } else {
      return ctx.getDistCalc().area(this);
    }
  }

  @Override
  public Circle getBuffered(double distance, SpatialContext ctx) {
    return ctx.makeCircle(point, distance + radiusDEG);
  }

  public boolean contains(double x, double y) {
    return ctx.getDistCalc().within(point, x, y, radiusDEG);
  }

  @Override
  public boolean hasArea() {
    return radiusDEG > 0;
  }

  /**
   * Note that the bounding box might contain a minX that is > maxX, due to WGS84 dateline.
   */
  @Override
  public Rectangle getBoundingBox() {
    return enclosingBox;
  }

  @Override
  public SpatialRelation relate(Shape other) {
//This shortcut was problematic in testing due to distinctions of CONTAINS/WITHIN for no-area shapes (lines, points).
//    if (distance == 0) {
//      return point.relate(other,ctx).intersects() ? SpatialRelation.WITHIN : SpatialRelation.DISJOINT;
//    }
    if (isEmpty() || other.isEmpty())
      return SpatialRelation.DISJOINT;
    if (other instanceof Point) {
      return relate((Point) other);
    }
    if (other instanceof Rectangle) {
      return relate((Rectangle) other);
    }
    if (other instanceof Circle) {
      return relate((Circle) other);
    }
    return other.relate(this).transpose();
  }

  public SpatialRelation relate(Point point) {
    return contains(point.getX(),point.getY()) ? SpatialRelation.CONTAINS : SpatialRelation.DISJOINT;
  }

  public SpatialRelation relate(Rectangle r) {
    //Note: Surprisingly complicated!

    //--We start by leveraging the fact we have a calculated bbox that is "cheaper" than use of DistanceCalculator.
    final SpatialRelation bboxSect = enclosingBox.relate(r);
    if (bboxSect == SpatialRelation.DISJOINT || bboxSect == SpatialRelation.WITHIN)
      return bboxSect;
    else if (bboxSect == SpatialRelation.CONTAINS && enclosingBox.equals(r))//nasty identity edge-case
      return SpatialRelation.WITHIN;
    //bboxSect is INTERSECTS or CONTAINS
    //The result can be DISJOINT, CONTAINS, or INTERSECTS (not WITHIN)

    return relateRectanglePhase2(r, bboxSect);
  }

  protected SpatialRelation relateRectanglePhase2(final Rectangle r, SpatialRelation bboxSect) {
    // DOES NOT WORK WITH GEO CROSSING DATELINE OR WORLD-WRAP. Other methods handle such cases.

    //At this point, the only thing we are certain of is that circle is *NOT* WITHIN r, since the
    // bounding box of a circle MUST be within r for the circle to be within r.

    //Quickly determine if they are DISJOINT or not.
    // Find the closest & farthest point to the circle within the rectangle
    final double closestX, farthestX;
    final double xAxis = getXAxis();
    if (xAxis < r.getMinX()) {
      closestX = r.getMinX();
      farthestX = r.getMaxX();
    } else if (xAxis > r.getMaxX()) {
      closestX = r.getMaxX();
      farthestX = r.getMinX();
    } else {
      closestX = xAxis; //we don't really use this value but to check this condition
      farthestX = r.getMaxX() - xAxis > xAxis - r.getMinX() ? r.getMaxX() : r.getMinX();
    }

    final double closestY, farthestY;
    final double yAxis = getYAxis();
    if (yAxis < r.getMinY()) {
      closestY = r.getMinY();
      farthestY = r.getMaxY();
    } else if (yAxis > r.getMaxY()) {
      closestY = r.getMaxY();
      farthestY = r.getMinY();
    } else {
      closestY = yAxis; //we don't really use this value but to check this condition
      farthestY = r.getMaxY() - yAxis > yAxis - r.getMinY() ? r.getMaxY() : r.getMinY();
    }

    //If r doesn't overlap an axis, then could be disjoint. Test closestXY
    if (xAxis != closestX && yAxis != closestY) {
      if (!contains(closestX, closestY))
        return SpatialRelation.DISJOINT;
    } // else CAN'T be disjoint if spans axis because earlier bbox check ruled that out

    //Now, we know it's *NOT* DISJOINT and it's *NOT* WITHIN either.
    // Does circle CONTAINS r or simply intersect it?

    //If circle contains r, then its bbox MUST also CONTAIN r.
    if (bboxSect != SpatialRelation.CONTAINS)
      return SpatialRelation.INTERSECTS;

    //If the farthest point of r away from the center of the circle is contained, then all of r is
    // contained.
    if (!contains(farthestX, farthestY))
      return SpatialRelation.INTERSECTS;

    //geodetic detection of farthest Y when rect crosses x axis can't be reliably determined, so
    // check other corner too, which might actually be farthest
    if (point.getY() != getYAxis()) {//geodetic
      if (yAxis == closestY) {//r crosses north to south over x axis (confusing)
        double otherY = (farthestY == r.getMaxY() ? r.getMinY() : r.getMaxY());
        if (!contains(farthestX, otherY))
          return SpatialRelation.INTERSECTS;
      }
    }
   
    return SpatialRelation.CONTAINS;
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

  public SpatialRelation relate(Circle circle) {
    double crossDist = ctx.getDistCalc().distance(point, circle.getCenter());
    double aDist = radiusDEG, bDist = circle.getRadius();
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
    return "Circle(" + point + ", d=" + radiusDEG + "°)";
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
