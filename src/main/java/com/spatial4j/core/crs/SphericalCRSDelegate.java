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
 * Various coordinate utility methods within a defined spherical coordinate reference system
 * (currently WGS84 lat/lon).  An instance of this class is created through the
 * {@link com.spatial4j.core.context.SpatialContextFactory}
 * todo expanded to accommodate Proj4J CoordinateReferenceSystem
 */
public class SphericalCRSDelegate extends AbstractCRSDelegate {

  private double CIRCUMFERENCE = 360.0;

  public SphericalCRSDelegate() {
    MIN_X = -180.0;
    MIN_Y = -90.0;
    MAX_X = 180.0;
    MAX_Y = 90.0;
    X_RANGE = 360.0;
    Y_RANGE = 180.0;
  }

  public double normLonDEG(double lon_deg) {
    return normalizeX(lon_deg);
  }

  public double normLatDEG(double lat_deg) {
    return normalizeY(lat_deg);
  }

  /**
   * Puts in range MIN_X <= x <= MAX_X.
   */
  public double normalizeX(double x) {
    if (x >= MIN_X && x <= MAX_X)
      return x;//common case, and avoids slight double precision shifting
    double off = (x + MAX_X) % CIRCUMFERENCE;
    if (off < 0)
      return MAX_X + off;
    else if (off == 0 && x > 0)
      return MAX_X;
    else
      return MIN_X + off;
  }

  /**
   * Puts in range MIN_Y <= y <= MAX_Y.
   */
  @Override
  public double normalizeY(double y) {
    if (y >= MIN_Y && y <= MAX_Y)
      return y;//common case, and avoids slight double precision shifting
    double off = Math.abs((y + MAX_Y) % CIRCUMFERENCE);
    return (off <= Y_RANGE ? off : CIRCUMFERENCE -off) - MAX_Y;
  }

  /**
   * Normalizes a {@link com.spatial4j.core.shape.Point} to fit within the standard world bounds
   * taking into account antipodal points for pole crossing.
   * If latitude is normalized from -90 < lat > 90, to -90 <= lat <= 90 then longitude will
   * normalized to its antipodal point using a 180 shift, <code>normPoint</code> should be used
   * in place of <code>normLonDEG</code> and <code>normLatDEG</code> when correct pole crossing
   * is required.
   */
  @Override
  public Point normalizePoint(Point pt) {
    double y = pt.getY();
    double x = pt.getX();
    boolean normalized = false;

    if (y>MAX_Y || y<MIN_Y) {
      normalized = true;
      // handle world wrapping and shift result 90 degrees
      y += MAX_Y;

      // correct mod operator for negative numbers
      double off = (y<0) ? ((y % CIRCUMFERENCE + CIRCUMFERENCE) % y) : y % CIRCUMFERENCE;
      // antipodal shift of longitude needed for y results > 180 degrees
      if (off > Y_RANGE) {
        // re-normalize the latitude and antipodal shift longitude
        y = (CIRCUMFERENCE - off) - MAX_Y;
        x += Y_RANGE;
      } else {
        y = off - MAX_Y;
      }
    }

    if (x > MAX_X  || x <= MIN_X) {
      if (!normalized) normalized = true;
      x = this.normalizeX(x);
    }

    if (normalized) {
      pt.reset(x, y);
    }
    return pt;
  }

}
