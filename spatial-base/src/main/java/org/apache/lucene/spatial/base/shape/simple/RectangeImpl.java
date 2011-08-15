/**
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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.*;

/**
 * A simple Rectangle implementation that also supports a longitudinal wrap-around. When minX > maxX, this will assume it is world coordinates that cross the
 * date line using degrees
 */
public class RectangeImpl implements Rectangle {

  private double minX;
  private double maxX;
  private double minY;
  private double maxY;

  public RectangeImpl(double minX, double maxX, double minY, double maxY) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  @Override
  public boolean hasArea() {
    return getWidth() > 0 && getHeight() > 0;
  }

  @Override
  public double getArea() {
    // CrossedDateline = true;
    if (minX > maxX) {
      return Math.abs(maxX + 360.0 - minX) * Math.abs(maxY - minY);
    }
    return Math.abs(maxX - minX) * Math.abs(maxY - minY);
  }

  @Override
  public boolean getCrossesDateLine() {
    return (minX > maxX);
  }

  @Override
  public double getHeight() {
    return maxY - minY;
  }

  @Override
  public double getWidth() {
    double w = maxX - minX;
    if (w > 360)
      return w - 360;
    else if (w < 0 || (w == 0 && minX != maxX))
      return w + 360;
    return w;
  }

  @Override
  public double getMaxX() {
    return maxX;
  }

  @Override
  public double getMaxY() {
    return maxY;
  }

  @Override
  public double getMinX() {
    return minX;
  }

  @Override
  public double getMinY() {
    return minY;
  }

  @Override
  public boolean hasSize() {
    return maxX != minX && maxY != minY;
  }

  @Override
  public Rectangle getBoundingBox() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext context) {
    if (other instanceof Point) {
      Point point = (Point) other;
      if (point.getY() > getMaxY() || point.getY() < getMinY() ||
          (minX > maxX ?
              (point.getX() < minX && point.getX() > maxX)
              : (point.getX() < minX || point.getX() > maxX) ))
        return IntersectCase.OUTSIDE;
      return IntersectCase.CONTAINS;
    }

    if (! (other instanceof Rectangle) ) {
      return other.intersect(this,context).transpose();
    }

    //Must be another rectangle...

    Rectangle ext = other.getBoundingBox();
    if (ext.getMinX() > maxX ||
        ext.getMaxX() < minX ||
        ext.getMinY() > maxY ||
        ext.getMaxY() < minY) {
      return IntersectCase.OUTSIDE;
    }

    if (ext.getMinX() >= minX &&
        ext.getMaxX() <= maxX &&
        ext.getMinY() >= minY &&
        ext.getMaxY() <= maxY) {
      return IntersectCase.CONTAINS;
    }

    if (minX >= ext.getMinY() &&
        maxX <= ext.getMaxX() &&
        minY >= ext.getMinY() &&
        maxY <= ext.getMaxY()) {
      return IntersectCase.WITHIN;
    }
    return IntersectCase.INTERSECTS;
  }

  @Override
  public String toString() {
    return "Rect(minX=" + minX + ",maxX=" + maxX + ",minY=" + minY + ",maxY=" + maxY + ")";
  }

  @Override
  public Point getCenter() {
    final double y = getHeight() / 2 + minY;
    double x = getWidth() / 2 + minX;
    if (x > 180)
      x -= 360;
    else if (x < -180)
      x += 360;
    return new PointImpl(x, y);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    RectangeImpl rhs = (RectangeImpl) obj;
    return new EqualsBuilder()
                  .appendSuper(super.equals(obj))
                  .append(minX, rhs.minX)
                  .append(minY, rhs.minY)
                  .append(maxX, rhs.maxX)
                  .append(maxY, rhs.maxY)
                  .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(41, 37).
    append(minX).append(minY).
    append(maxX).append(maxY).
      toHashCode();
  }
}
