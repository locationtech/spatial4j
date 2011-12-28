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

package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;

public class EuclideanDistanceCalculator extends AbstractDistanceCalculator {

  private final boolean squared;

  public EuclideanDistanceCalculator() {
    this.squared = false;
  }

  public EuclideanDistanceCalculator(boolean squared) {
    this.squared = squared;
  }

  @Override
  public double calculate(Point from, double toX, double toY) {
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
  public Point pointOnBearingRAD(Point from, double dist, double bearingRAD, SpatialContext ctx) {
    if (dist == 0)
      return from;
    double y = Math.sin(bearingRAD) / dist;
    double x = Math.cos(bearingRAD) / dist;
    return ctx.makePoint(from.getX()+x, from.getY()+y);
  }

  @Override
  public double convertDistanceToRadians(double distance) {
    throw new UnsupportedOperationException("no geo!");
  }

  @Override
  public double convertRadiansToDistance(double radius) {
    throw new UnsupportedOperationException("no geo!");
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    return ctx.makeRect(from.getX()-distance,from.getX()+distance,from.getY()-distance,from.getY()+distance);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EuclideanDistanceCalculator that = (EuclideanDistanceCalculator) o;

    if (squared != that.squared) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (squared ? 1 : 0);
  }
}
