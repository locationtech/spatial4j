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

import static com.spatial4j.core.shape.SpatialRelation.*;

/*
        Created by Chris Pavlicek (varsis)
        January 13th, 2014
 */

/*
    GeoBufferedLine represents a geodetic line.
 */
public class GeoBufferedLine {

  // Points will need to be represented in Longitude and Latitude.
  // Should enforce boundaries

   private SpatialContext ctx = null;
   final boolean bufExtend;

    private final Point pA;
    private final Point pB;
    private final PointImpl midPoint;

    private final double buf;

    double radius = 10;

  /**
   * Creates a geodetic buffered line from pA to pB. The buffer extends on both sides of
   * the line, making the width 2x the buffer. The buffer extends out from
   * pA & pB, making the line in effect 2x the buffer longer than pA to pB.
   *
   * @param pA  start point
   * @param pB  end point
   * @param buf the buffer distance in degrees
   * @param ctx SpatialContext for the line
   * @param bufExtended True if buffer should extend beyond point
   */
  public GeoBufferedLine(Point pA, Point pB, double buf, SpatialContext ctx, boolean bufExtended) {
    // Check context if it is geo
    assert ctx.isGeo();

    // The context we are using if we for some reason need it.
    this.setContext(ctx);


      /**
       * If true, buf should bump-out from the pA & pB, in effect
       *                  extending the line a little.
       */
    this.bufExtend = bufExtended;

    this.pA = pA;
    this.pB = pB;

    this.buf = buf;

    // Gets the center point
    midPoint =  new PointImpl((pA.getX() + pB.getX())/(double)2, (pA.getY() + pB.getY())/(double)2, null);

    // Find the Length of pA to midPoint
    // sqrt((x2 - x1)^2 + (y2-y1)^2)
    double deltaX = pB.getX() - pA.getX();
    double deltaY = pB.getY() - pB.getY();

    // Gives us the distance from pA to pB without curvature
    double verticalDistance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

    /*
            Given a Isosceles triangle we can
            figure out the angle between pA and pB
            and figure out the distance
            with curvature into account.
     */

    double halfVerticalDistance = verticalDistance/(double)2;
    double arcRadius = (double)2 * Math.asin(halfVerticalDistance/radius);

    // we now have the length of the curve
    double length = (arcRadius * Math.PI * radius) / (180);

  }

    public void setContext(SpatialContext ctx) {
        this.ctx = ctx;
    }

    public SpatialContext getContext() {
        return this.ctx;
    }
}

