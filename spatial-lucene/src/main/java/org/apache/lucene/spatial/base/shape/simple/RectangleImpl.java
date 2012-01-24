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

  //TODO change to West South East North to be more consistent with OGC?
  public RectangleImpl(double minX, double maxX, double minY, double maxY) {
    //We assume any normalization / validation of params already occurred.
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    assert minY <= maxY;
  }

  /** Copy constructor. */
  public RectangleImpl(Rectangle r) {
    this(r.getMinX(),r.getMaxX(),r.getMinY(),r.getMaxY());
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
      return intersect((Point) other, ctx);
    }
    if (other instanceof Rectangle) {
      return intersect((Rectangle)other, ctx);
    }
    return other.intersect(this, ctx).transpose();
  }

  public IntersectCase intersect(Point point, SpatialContext ctx) {
    if (point.getY() > getMaxY() || point.getY() < getMinY() ||
        (getCrossesDateLine() ?
            (point.getX() < minX && point.getX() > maxX)
            : (point.getX() < minX || point.getX() > maxX) ))
      return IntersectCase.OUTSIDE;
    return IntersectCase.CONTAINS;
  }

  public IntersectCase intersect(Rectangle rect, SpatialContext ctx) {
    IntersectCase yIntersect = intersect_yRange(rect.getMinY(),rect.getMaxY(),ctx);
    if (yIntersect == IntersectCase.OUTSIDE)
      return IntersectCase.OUTSIDE;

    IntersectCase xIntersect = intersect_xRange(rect.getMinX(),rect.getMaxX(),ctx);
    if (xIntersect == IntersectCase.OUTSIDE)
      return IntersectCase.OUTSIDE;

    if (xIntersect == yIntersect)//in agreement
      return xIntersect;

    //if one side is equal, return the other
    if (getMinX() == rect.getMinX() && getMaxX() == rect.getMaxX())
      return yIntersect;
    if (getMinY() == rect.getMinY() && getMaxY() == rect.getMaxY())
      return xIntersect;

    return IntersectCase.INTERSECTS;
  }

  public IntersectCase intersect_yRange(double ext_minY, double ext_maxY, SpatialContext ctx) {
    if (ext_minY > maxY || ext_maxY < minY) {
      return IntersectCase.OUTSIDE;
    }

    if (ext_minY >= minY && ext_maxY <= maxY) {
      return IntersectCase.CONTAINS;
    }

    if (ext_minY <= minY && ext_maxY >= maxY) {
      return IntersectCase.WITHIN;
    }
    return IntersectCase.INTERSECTS;
  }

  @Override
  public IntersectCase intersect_xRange(double ext_minX, double ext_maxX, SpatialContext ctx) {
    //For ext & this we have local minX and maxX variable pairs. We rotate them so that minX <= maxX
    double minX = this.minX;
    double maxX = this.maxX;
    if (ctx.isGeo()) {
      //the 360 check is an edge-case for complete world-wrap
      double ext_width = ext_maxX - ext_minX;
      if (ext_width < 0)//this logic unfortunately duplicates getWidth()
        ext_width += 360;

      if (ext_width < 360) {
        ext_maxX = ext_minX + ext_width;
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

    if (ext_minX > maxX || ext_maxX < minX ) {
      return IntersectCase.OUTSIDE;
    }

    if (ext_minX >= minX && ext_maxX <= maxX ) {
      return IntersectCase.CONTAINS;
    }

    if (ext_minX <= minX && ext_maxX >= maxX ) {
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
      x = DistanceUtils.normLonDEG(x);
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
