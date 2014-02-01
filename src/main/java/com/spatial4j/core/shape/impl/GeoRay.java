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
 * INERNAL: A infinite line in geo
 * Public for test access.
 */

/**
 * Created by Chris Pavlicek
 * Jan 21 2014
 */

public class GeoRay {
  // This is a normal line implementation : y = slope * x + yIntercept
  // if slope is 0 line is parallel to the equator.
  private final double slope;

  // Where x = 0
  private final double yIntercept;

  // This value is 0 = slope * x + intercept
  // (-yIntercept/slope) = x
  private final double xIntercept;

  private final double distDenomInv;//cached: 1 / Math.sqrt(slope * slope + 1)

  private final double buf;

  /**
   * Represents an infinite line in geodesic terms,
   * the slope is represented in a flat surface model.
   * (0,0) is UTC
   *
   * @param slope
   * @param point
   */
  GeoRay(double slope, Point point, double buf) {

    // Check the slope
    assert !Double.isNaN(slope);

    // for use in calculating distance and intersections
    this.buf = buf;

    this.slope = slope;

    // is the slope is infinite, we have a vertical line
    if (Double.isInfinite(slope)) {
      // We either intersect y at all points, or none at all.
      xIntercept = point.getX();

      if(xIntercept == 0) {
        // All values intersect
        yIntercept = Double.POSITIVE_INFINITY;
      } else {
        // Undefined for yIntercept
        yIntercept = Double.NaN;
      }
      distDenomInv = Double.NaN;
    } else {

      // Get y Intercept for point
      yIntercept = point.getY() - slope * point.getX();

      //calculate the x Intercept using the y intercept.
      xIntercept = (-1.0 * yIntercept) / slope;

      distDenomInv = 1 / Math.sqrt(slope * slope + 1);
    }



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

  // Original version is for a straight line, however now the buffer is curved.
  // The buffer should be specified in degrees, and should not require any further
  // calculations. (Since we are not finding a distance that is from 2 points and abstracting
  // to a flat surface)
  boolean contains(Point p) {
    return (distanceUnbuffered(p) <= buf);
  }

  /** INTERNAL AKA lineToPointDistance */
  public double distanceUnbuffered(Point c) {

    // Vertical line.
    if (Double.isInfinite(slope)) {
      return Math.abs(c.getX() - xIntercept);
    } else {
      // We have a line of some sort
      // Use the Shortest distance to line, point formula
      // See link for more information.
      // http://math.ucsd.edu/~wgarner/math4c/derivations/distance/distptline.htm
      double num = Math.abs(c.getY() - slope * c.getX() - xIntercept);
      return num * distDenomInv;
    }
  }

  /** INTERNAL: AKA lineToPointQuadrant */
  // Same as RayLine
  // Should still work, but mapped on a sphere
  // vs a euclidean plane.
  public int quadrant(Point c) {
    //check vertical line case 1st
    if (Double.isInfinite(slope)) {
      //when slope is infinite, intercept is x intercept instead of y
      return c.getX() > xIntercept ? 1 : 2; //4 : 3 would work too
    }
    //(below will work for slope==0 horizontal line too)
    //is c above or below the line
    double yAtCinLine = slope * c.getX() + xIntercept;
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

  public double getEquatorIntercept() {
    return xIntercept;
  }

  public double getPrimeMeridianIntercept() {
    return yIntercept;
  }

  /** 1 / Math.sqrt(slope * slope + 1) */
  public double getDistDenomInv() {
    return distDenomInv;
  }

  @Override
  public String toString() {
    return "RayLine{" +
        ", equatorIntercept=" + xIntercept +
        ", primeMeridianIntercept=" + yIntercept +
        ", slope=" + slope +
        '}';
  }
}
