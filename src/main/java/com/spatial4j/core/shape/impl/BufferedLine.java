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
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;

/**
 * INTERNAL: A line between two points with a buffer distance extending in every direction. By
 * contrast, an un-buffered line covers no area and as such is extremely unlikely to intersect with
 * a point. BufferedLine isn't yet aware of geodesics (e.g. the dateline); it operates in Euclidean
 * space.
 */
public class BufferedLine implements Shape {

  private final Point pA, pB;
  private final double buf;
  private final Rectangle bbox;
  /**
   * the primary line; passes through pA & pB
   */
  private final InfBufLine linePrimary;
  /**
   * perpendicular to the primary line, centered between pA & pB
   */
  private final InfBufLine linePerp;

  /**
   * Creates a buffered line from pA to pB. The buffer extends on both sides of
   * the line, making the width 2x the buffer. The buffer extends out from
   * pA & pB, making the line in effect 2x the buffer longer than pA to pB.
   *
   * @param pA  start point
   * @param pB  end point
   * @param buf the buffer distance in degrees
   * @param ctx
   */
  public BufferedLine(Point pA, Point pB, double buf, SpatialContext ctx) {
    assert buf >= 0;//TODO support buf=0 via another class ?

    /**
     * If true, buf should bump-out from the pA & pB, in effect
     *                  extending the line a little.
     */
    final boolean bufExtend = true;//TODO support false and make this a
    // parameter

    this.pA = pA;
    this.pB = pB;
    this.buf = buf;

    double deltaY = pB.getY() - pA.getY();
    double deltaX = pB.getX() - pA.getX();

    PointImpl center = new PointImpl(pA.getX() + deltaX / 2,
        pA.getY() + deltaY / 2, null);

    double perpExtent = bufExtend ? buf : 0;

    if (deltaX == 0 && deltaY == 0) {
      linePrimary = new InfBufLine(0, center, buf);
      linePerp = new InfBufLine(Double.POSITIVE_INFINITY, center, buf);
    } else {
      linePrimary = new InfBufLine(deltaY / deltaX, center, buf);
      double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      linePerp = new InfBufLine(-deltaX / deltaY, center,
          length / 2 + perpExtent);
    }

    double minY, maxY;
    double minX, maxX;
    if (deltaX == 0) { // vertical
      if (pA.getY() <= pB.getY()) {
        minY = pA.getY();
        maxY = pB.getY();
      } else {
        minY = pB.getY();
        maxY = pA.getY();
      }
      minX = pA.getX() - buf;
      maxX = pA.getX() + buf;
      minY = minY - perpExtent;
      maxY = maxY + perpExtent;

    } else {
      if (!bufExtend) {
        throw new UnsupportedOperationException("TODO");
        //solve for B & A (C=buf), one is buf-x, other is buf-y.
      }

      //Given a right triangle of A, B, C sides, C (hypotenuse) ==
      // buf, and A + B == the bounding box offset from pA & pB in x & y.
      double bboxBuf = buf * (1 + Math.abs(linePrimary.getSlope()))
          * linePrimary.getDistDenomInv();
      assert bboxBuf >= buf && bboxBuf <= buf * 1.5;

      if (pA.getX() <= pB.getX()) {
        minX = pA.getX() - bboxBuf;
        maxX = pB.getX() + bboxBuf;
      } else {
        minX = pB.getX() - bboxBuf;
        maxX = pA.getX() + bboxBuf;
      }
      if (pA.getY() <= pB.getY()) {
        minY = pA.getY() - bboxBuf;
        maxY = pB.getY() + bboxBuf;
      } else {
        minY = pB.getY() - bboxBuf;
        maxY = pA.getY() + bboxBuf;
      }

    }
    Rectangle bounds = ctx.getWorldBounds();

    bbox = ctx.makeRectangle(
        Math.max(bounds.getMinX(), minX),
        Math.min(bounds.getMaxX(), maxX),
        Math.max(bounds.getMinY(), minY),
        Math.min(bounds.getMaxY(), maxY));
  }

  @Override
  public boolean isEmpty() {
    return pA.isEmpty();
  }

  @Override
  public Shape getBuffered(double distance, SpatialContext ctx) {
    return new BufferedLine(pA, pB, buf + distance, ctx);
  }

  /**
   * Calls {@link DistanceUtils#calcLonDegreesAtLat(double, double)} given pA or pB's latitude;
   * whichever is farthest. It's useful to expand a buffer of a line segment when used in
   * a geospatial context to cover the desired area.
   */
  public static double expandBufForLongitudeSkew(Point pA, Point pB,
                                                 double buf) {
    double absA = Math.abs(pA.getY());
    double absB = Math.abs(pB.getY());
    double maxLat = Math.max(absA, absB);
    double newBuf = DistanceUtils.calcLonDegreesAtLat(maxLat, buf);
//    if (newBuf + maxLat >= 90) {
//      //TODO substitute spherical cap ?
//    }
    assert newBuf >= buf;
    return newBuf;
  }

  @Override
  public SpatialRelation relate(Shape other) {
    if (other instanceof Point)
      return contains((Point) other) ? CONTAINS : DISJOINT;
    if (other instanceof Rectangle)
      return relate((Rectangle) other);
    throw new UnsupportedOperationException();
  }

  public SpatialRelation relate(Rectangle r) {
    //Check BBox for disjoint & within.
    SpatialRelation bboxR = bbox.relate(r);
    if (bboxR == DISJOINT || bboxR == WITHIN)
      return bboxR;
    //Either CONTAINS, INTERSECTS, or DISJOINT

    Point scratch = new PointImpl(0, 0, null);
    Point prC = r.getCenter();
    SpatialRelation result = linePrimary.relate(r, prC, scratch);
    if (result == DISJOINT)
      return DISJOINT;
    SpatialRelation resultOpp = linePerp.relate(r, prC, scratch);
    if (resultOpp == DISJOINT)
      return DISJOINT;
    if (result == resultOpp)//either CONTAINS or INTERSECTS
      return result;
    return INTERSECTS;
  }

  public boolean contains(Point p) {
    //TODO check bbox 1st?
    return linePrimary.contains(p) && linePerp.contains(p);
  }

  public Rectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public boolean hasArea() {
    return buf > 0;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return linePrimary.getBuf() * linePerp.getBuf() * 4;
  }

  @Override
  public Point getCenter() {
    return getBoundingBox().getCenter();
  }

  public Point getA() {
    return pA;
  }

  public Point getB() {
    return pB;
  }

  public double getBuf() {
    return buf;
  }

  /**
   * INTERNAL
   */
  public InfBufLine getLinePrimary() {
    return linePrimary;
  }

  /**
   * INTERNAL
   */
  public InfBufLine getLinePerp() {
    return linePerp;
  }

  @Override
  public String toString() {
    return "BufferedLine(" + pA + ", " + pB + " b=" + buf + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BufferedLine that = (BufferedLine) o;

    if (Double.compare(that.buf, buf) != 0) return false;
    if (!pA.equals(that.pA)) return false;
    if (!pB.equals(that.pB)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = pA.hashCode();
    result = 31 * result + pB.hashCode();
    temp = buf != +0.0d ? Double.doubleToLongBits(buf) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
