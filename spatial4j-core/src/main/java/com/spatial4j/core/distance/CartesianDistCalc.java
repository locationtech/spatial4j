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

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

/**
 * Calculates based on Euclidean / Cartesian 2d plane.
 */
public class CartesianDistCalc extends AbstractDistanceCalculator {

  private final boolean squared;

  public CartesianDistCalc() {
    this.squared = false;
  }

  /**
   * @param squared Set to true to have {@link #distance(com.spatial4j.core.shape.Point, com.spatial4j.core.shape.Point)}
   *                return the square of the correct answer. This is a
   *                performance optimization used when sorting in which the
   *                actual distance doesn't matter so long as the sort order is
   *                consistent.
   */
  public CartesianDistCalc(boolean squared) {
    this.squared = squared;
  }

  @Override
  public double distance(Point from, double toX, double toY) {
    double result = 0;

    double v = from.getX() - toX;
    result += (v * v);

    v = from.getY() - toY;
    result += (v * v);

    if( squared )
      return result;

    return Math.sqrt(result);
  }

  @Override
  public Point pointOnBearing(Point from, double dist, double bearingDEG, SpatialContext ctx) {
    if (dist == 0)
      return from;
    double bearingRAD = DistanceUtils.toRadians(bearingDEG);
    double x = Math.sin(bearingRAD) * dist;
    double y = Math.cos(bearingRAD) * dist;
    return ctx.makePoint(from.getX()+x, from.getY()+y);
  }

  @Override
  public double distanceToDegrees(double distance) {
    throw new UnsupportedOperationException("no geo!");
  }

  @Override
  public double degreesToDistance(double degrees) {
    throw new UnsupportedOperationException("no geo!");
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    return ctx.makeRect(from.getX()-distance,from.getX()+distance,from.getY()-distance,from.getY()+distance);
  }

  @Override
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distance, SpatialContext ctx) {
    return from.getY();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CartesianDistCalc that = (CartesianDistCalc) o;

    if (squared != that.squared) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (squared ? 1 : 0);
  }
}
