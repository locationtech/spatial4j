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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.apache.lucene.spatial.base.shape.*;

/**
 * A simple Rectangle implementation that also supports a longitudinal wrap-around. When minX > maxX, this will assume
 * it is world coordinates that cross the date line using degrees.
 * Immutable & threadsafe.
 */
public class RectangleImpl implements Rectangle {

  private final double minX;
  private final double maxX;
  private final double minY;
  private final double maxY;

  public RectangleImpl(double minX, double maxX, double minY, double maxY) {
    //We assume any normalization / validation of params already occurred.
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    assert minY <= maxY;
  }

  @Override
  public boolean hasArea() {
    return maxX != minX && maxY != minY;
  }

  @Override
  public double getArea() {
    return getWidth() * getHeight();
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
    if (w < 0) {//only true when minX > maxX (WGS84 assumed)
      w += 360;
      assert w >= 0;
    }
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
  public Rectangle getBoundingBox() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext ctx) {
    if (other instanceof Point) {
      Point point = (Point) other;
      if (point.getY() > getMaxY() || point.getY() < getMinY() ||
          (getCrossesDateLine() ?
              (point.getX() < minX && point.getX() > maxX)
              : (point.getX() < minX || point.getX() > maxX) ))
        return IntersectCase.OUTSIDE;
      return IntersectCase.CONTAINS;
    }

    if (! (other instanceof Rectangle) ) {
      return other.intersect(this, ctx).transpose();
    }

    //Must be another rectangle...

    Rectangle ext = other.getBoundingBox();

    //For ext & this we have local minX and maxX variable pairs. We rotate them so that minX <= maxX
    double ext_minX = ext.getMinX();
    double ext_maxX = ext.getMaxX();
    double minX = this.minX;
    double maxX = this.maxX;
    if (ctx.isGeo()) {
      //the 360 check is an edge-case for complete world-wrap
      if (ext.getWidth() < 360) {
        ext_maxX = ext_minX + ext.getWidth();
      } else {
        ext_maxX = 180+360;
      }

      if (getWidth() < 360) {
        maxX = minX + getWidth();
      } else {
        maxX = 180+360;
      }

      if (maxX < ext_minX) {
        minX += 360;
        maxX += 360;
      } else if (ext_maxX < minX) {
        ext_minX += 360;
        ext_maxX += 360;
      }
    }

    if (ext_minX > maxX ||
        ext_maxX < minX ||
        ext.getMinY() > maxY ||
        ext.getMaxY() < minY) {
      return IntersectCase.OUTSIDE;
    }

    if (ext_minX >= minX &&
        ext_maxX <= maxX &&
        ext.getMinY() >= minY &&
        ext.getMaxY() <= maxY) {
      return IntersectCase.CONTAINS;
    }

    if (ext_minX <= minX &&
        ext_maxX >= maxX &&
        ext.getMinY() <= minY &&
        ext.getMaxY() >= maxY) {
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
    if (minX > maxX)//WGS84
      x = DistanceUtils.normLonDeg(x);
    return new PointImpl(x, y);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    RectangleImpl rhs = (RectangleImpl) obj;
    return new EqualsBuilder()
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
