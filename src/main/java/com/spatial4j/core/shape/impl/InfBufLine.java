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

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;

import static com.spatial4j.core.shape.SpatialRelation.*;

/**
 * INERNAL: A buffered line of infinite length.
 * Public for test access.
 */
public class InfBufLine {

  //TODO consider removing support for vertical line -- let caller
  // do something else.  BufferedLine could have a factory method
  // that returns a rectangle, for example.

  // line: y = slope * x + intercept

  private final double slope;//can be infinite for vertical line
  //if slope is infinite, this is x intercept, otherwise y intercept
  private final double intercept;

  private final double buf;

  private final double distDenomInv;//cached: 1 / Math.sqrt(slope * slope + 1)

  InfBufLine(double slope, Point point, double buf) {
    assert !Double.isNaN(slope);
    this.slope = slope;
    if (Double.isInfinite(slope)) {
      intercept = point.getX();
      distDenomInv = Double.NaN;
    } else {
      intercept = point.getY() - slope * point.getX();
      distDenomInv = 1 / Math.sqrt(slope * slope + 1);
    }
    this.buf = buf;
  }

  SpatialRelation relate(Rectangle r, Point prC, Point scratch) {
    assert r.getCenter().equals(prC);

    int cQuad = quadrant(prC);

    Point nearestP = scratch;
    cornerByQuadrant(r, oppositeQuad[cQuad], nearestP);
    boolean nearestContains = contains(nearestP);

    if (nearestContains) {
      Point farthestP = scratch;
      nearestP = null;//just to be safe (same scratch object)
      cornerByQuadrant(r, cQuad, farthestP);
      boolean farthestContains = contains(farthestP);
      if (farthestContains)
        return CONTAINS;
      return INTERSECTS;
    } else {// not nearestContains
      if (quadrant(nearestP) == cQuad)
        return DISJOINT;//out of buffer on same side as center
      return INTERSECTS;//nearest & farthest points straddle the line
    }
  }

  boolean contains(Point p) {
    return (distanceUnbuffered(p) <= buf);
  }

  /** AKA lineToPointDistance */
  double distanceUnbuffered(Point c) {
    if (Double.isInfinite(slope))
      return Math.abs(c.getX() - intercept);
    // http://math.ucsd.edu/~wgarner/math4c/derivations/distance/distptline.htm
    double num = Math.abs(c.getY() - slope * c.getX() - intercept);
    return num * distDenomInv;
  }

//  /** Amount to add or subtract to intercept to indicate where the
//   * buffered line edges cross the y axis.
//   * @return
//   */
//  double interceptBuffOffset() {
//    if (Double.isInfinite(slope))
//      return slope;
//    if (buf == 0)
//      return 0;
//    double slopeDivBuf = slope / buf;
//    return Math.sqrt(buf*buf + slopeDivBuf*slopeDivBuf);
//  }

  /** AKA lineToPointQuadrant */
  int quadrant(Point c) {
    //check vertical line case 1st
    if (Double.isInfinite(slope)) {
      //when slope is infinite, intercept is x intercept instead of y
      return c.getX() > intercept ? 1 : 2; //4 : 3 would work too
    }
    //(below will work for slope==0 horizontal line too)
    //is c above or below the line
    double yAtCinLine = slope * c.getX() + intercept;
    boolean above = c.getY() >= yAtCinLine;
    if (slope > 0) {
      //if slope is a forward slash, then result is 2 | 4
      return above ? 2 : 4;
    } else {
      //if slope is a backward slash, then result is 1 | 3
      return above ? 1 : 3;
    }
  }

  //TODO ? Use an Enum for quadrant?

  /* quadrants 1-4: NE, NW, SW, SE. */
  private static final int[] oppositeQuad= {-1,3,4,1,2};

  public static void cornerByQuadrant(Rectangle r, int cornerQuad, Point out) {
    double x = (cornerQuad == 1 || cornerQuad == 4) ? r.getMaxX() : r.getMinX();
    double y = (cornerQuad == 1 || cornerQuad == 2) ? r.getMaxY() : r.getMinY();
    out.reset(x, y);
  }

  public double getSlope() {
    return slope;
  }

  public double getIntercept() {
    return intercept;
  }

  public double getBuf() {
    return buf;
  }

  /** 1 / Math.sqrt(slope * slope + 1) */
  public double getDistDenomInv() {
    return distDenomInv;
  }

  @Override
  public String toString() {
    return "InfBufLine{" +
        "buf=" + buf +
        ", intercept=" + intercept +
        ", slope=" + slope +
        '}';
  }
}
