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

package com.spatial4j.core.crs;

import com.spatial4j.core.shape.Point;

/**
 * Various coordinate utility methods within a defined cartesian coordinate reference system
 * (e.g., ENU, ECEF, UTM).  An instance of this class is created through the
 * {@link com.spatial4j.core.context.SpatialContextFactory}
 * todo link to a Proj4J CoordinateReferenceSystem
 */
public class CartesianCRSDelegate extends AbstractCRSDelegate {

  /**
   * Default cartesian coordinate system uses standard axis wrapping
   * (e.g., similar to screen wrapping with origin at lower left)
   */
  public CartesianCRSDelegate() {
    this(0.0, 360.0, 0.0, 180.0);
  }

  public CartesianCRSDelegate(double minX, double maxX, double minY, double maxY) {
    this.MIN_X = minX;
    this.MAX_X = maxX;
    this.MIN_Y = minY;
    this.MAX_Y = maxY;
    this.X_RANGE = maxX - minX;
    this.Y_RANGE = maxY - maxY;
  }

  @Override
  public double normalizeX(double x) {
    if (x < MIN_X || x > MAX_X) {
      return ((x-MIN_X) % X_RANGE) + MIN_X;
    }
    return x;
  }

  @Override
  public double normalizeY(double y) {
    return normalizeX(y);
  }

  @Override
  public Point normalizePoint(Point p) {
    p.reset(normalizeX(p.getX()), normalizeY(p.getY()));
    return p;
  }
}
