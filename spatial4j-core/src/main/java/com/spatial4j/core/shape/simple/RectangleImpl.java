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
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.*;

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
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    if (other instanceof Point) {
      return relate((Point) other, ctx);
    }
    if (other instanceof Rectangle) {
      return relate((Rectangle) other, ctx);
    }
    return other.relate(this, ctx).transpose();
  }

  public SpatialRelation relate(Point point, SpatialContext ctx) {
    if (point.getY() > getMaxY() || point.getY() < getMinY() ||
        (getCrossesDateLine() ?
            (point.getX() < minX && point.getX() > maxX)
            : (point.getX() < minX || point.getX() > maxX) ))
      return SpatialRelation.DISJOINT;
    return SpatialRelation.CONTAINS;
  }

  public SpatialRelation relate(Rectangle rect, SpatialContext ctx) {
    SpatialRelation yIntersect = relate_yRange(rect.getMinY(), rect.getMaxY(), ctx);
    if (yIntersect == SpatialRelation.DISJOINT)
      return SpatialRelation.DISJOINT;

    SpatialRelation xIntersect = relate_xRange(rect.getMinX(), rect.getMaxX(), ctx);
    if (xIntersect == SpatialRelation.DISJOINT)
      return SpatialRelation.DISJOINT;

    if (xIntersect == yIntersect)//in agreement
      return xIntersect;

    //if one side is equal, return the other
    if (getMinX() == rect.getMinX() && getMaxX() == rect.getMaxX())
      return yIntersect;
    if (getMinY() == rect.getMinY() && getMaxY() == rect.getMaxY())
      return xIntersect;

    return SpatialRelation.INTERSECTS;
  }

  public SpatialRelation relate_yRange(double ext_minY, double ext_maxY, SpatialContext ctx) {
    if (ext_minY > maxY || ext_maxY < minY) {
      return SpatialRelation.DISJOINT;
    }

    if (ext_minY >= minY && ext_maxY <= maxY) {
      return SpatialRelation.CONTAINS;
    }

    if (ext_minY <= minY && ext_maxY >= maxY) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public SpatialRelation relate_xRange(double ext_minX, double ext_maxX, SpatialContext ctx) {
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
      return SpatialRelation.DISJOINT;
    }

    if (ext_minX >= minX && ext_maxX <= maxX ) {
      return SpatialRelation.CONTAINS;
    }

    if (ext_minX <= minX && ext_maxX >= maxX ) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RectangleImpl rectangle = (RectangleImpl) o;

    if (Double.compare(rectangle.maxX, maxX) != 0) return false;
    if (Double.compare(rectangle.maxY, maxY) != 0) return false;
    if (Double.compare(rectangle.minX, minX) != 0) return false;
    if (Double.compare(rectangle.minY, minY) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = minX != +0.0d ? Double.doubleToLongBits(minX) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = maxX != +0.0d ? Double.doubleToLongBits(maxX) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = minY != +0.0d ? Double.doubleToLongBits(minY) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = maxY != +0.0d ? Double.doubleToLongBits(maxY) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
