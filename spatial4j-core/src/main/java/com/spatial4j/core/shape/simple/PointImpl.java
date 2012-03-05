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

package com.spatial4j.core.shape.simple;

import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;


public class PointImpl implements Point {

  private final double x;
  private final double y;

  public PointImpl(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public double getX() {
    return x;
  }

  @Override
  public double getY() {
    return y;
  }
  @Override
  public Rectangle getBoundingBox() {
    return new RectangleImpl(x, x, y, y);
  }

  @Override
  public PointImpl getCenter() {
    return this;
  }

  @Override
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    if (other instanceof Point)
      return this.equals(other) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
    return other.relate(this, ctx).transpose();
  }

  @Override
  public boolean hasArea() {
    return false;
  }

  @Override
  public String toString() {
    return "Pt(x="+x+",y="+y+")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PointImpl point = (PointImpl) o;

    if (Double.compare(point.x, x) != 0) return false;
    if (Double.compare(point.y, y) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
