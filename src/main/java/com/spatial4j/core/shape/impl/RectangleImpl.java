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

/**
 * A simple Rectangle implementation that also supports a longitudinal
 * wrap-around. When minX > maxX, this will assume it is world coordinates that
 * cross the date line using degrees. Immutable & threadsafe.
 */
public class RectangleImpl implements Rectangle {

  private final SpatialContext ctx;
  private double minX;
  private double maxX;
  private double minY;
  private double maxY;

  /** A simple constructor without normalization / validation. */
  public RectangleImpl(double minX, double maxX, double minY, double maxY, SpatialContext ctx) {
    //TODO change to West South East North to be more consistent with OGC?
    this.ctx = ctx;
    reset(minX, maxX, minY, maxY);
  }

  /** A convenience constructor which pulls out the coordinates. */
  public RectangleImpl(Point lowerLeft, Point upperRight, SpatialContext ctx) {
    this(lowerLeft.getX(), upperRight.getX(),
        lowerLeft.getY(), upperRight.getY(), ctx);
  }

  /** Copy constructor. */
  public RectangleImpl(Rectangle r, SpatialContext ctx) {
    this(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY(), ctx);
  }

  @Override
  public void reset(double minX, double maxX, double minY, double maxY) {
    assert ! isEmpty();
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    assert minY <= maxY || Double.isNaN(minY) : "minY, maxY: "+minY+", "+maxY;
  }

  @Override
  public boolean isEmpty() {
    return Double.isNaN(minX);
  }

  @Override
  public Rectangle getBuffered(double distance, SpatialContext ctx) {
    if (ctx.isGeo()) {
      //first check pole touching, triggering a world-wrap rect
      if (maxY + distance >= 90) {
        return ctx.makeRectangle(-180, 180, Math.max(-90, minY - distance), 90);
      } else if (minY - distance <= -90) {
        return ctx.makeRectangle(-180, 180, -90, Math.min(90, maxY + distance));
      } else {
        //doesn't touch pole
        double latDistance = distance;
        double closestToPoleY = (maxY - minY > 0) ? maxY : minY;
        double lonDistance = DistanceUtils.calcBoxByDistFromPt_deltaLonDEG(
            closestToPoleY, minX, distance);//lat,lon order
        //could still wrap the world though...
        if (lonDistance * 2 + getWidth() >= 360)
          return ctx.makeRectangle(-180, 180, minY - latDistance, maxY + latDistance);
        return ctx.makeRectangle(
            DistanceUtils.normLonDEG(minX - lonDistance),
            DistanceUtils.normLonDEG(maxX + lonDistance),
            minY - latDistance, maxY + latDistance);
      }
    } else {
      Rectangle worldBounds = ctx.getWorldBounds();
      double newMinX = Math.max(worldBounds.getMinX(), minX - distance);
      double newMaxX = Math.min(worldBounds.getMaxX(), maxX + distance);
      double newMinY = Math.max(worldBounds.getMinY(), minY - distance);
      double newMaxY = Math.min(worldBounds.getMaxY(), maxY + distance);
      return ctx.makeRectangle(newMinX, newMaxX, newMinY, newMaxY);
    }
  }

  @Override
  public boolean hasArea() {
    return maxX != minX && maxY != minY;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    if (ctx == null) {
      return getWidth() * getHeight();
    } else {
      return ctx.getDistCalc().area(this);
    }
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
  public SpatialRelation relate(Shape other) {
    if (isEmpty() || other.isEmpty())
      return SpatialRelation.DISJOINT;
    if (other instanceof Point) {
      return relate((Point) other);
    }
    if (other instanceof Rectangle) {
      return relate((Rectangle) other);
    }
    return other.relate(this).transpose();
  }

  public SpatialRelation relate(Point point) {
    if (point.getY() > getMaxY() || point.getY() < getMinY())
      return SpatialRelation.DISJOINT;
    //  all the below logic is rather unfortunate but some dateline cases demand it
    double minX = this.minX;
    double maxX = this.maxX;
    double pX = point.getX();
    if (ctx.isGeo()) {
      //unwrap dateline and normalize +180 to become -180
      double rawWidth = maxX - minX;
      if (rawWidth < 0) {
        maxX = minX + (rawWidth + 360);
      }
      //shift to potentially overlap
      if (pX < minX) {
        pX += 360;
      } else if (pX > maxX) {
        pX -= 360;
      } else {
        return SpatialRelation.CONTAINS;//short-circuit
      }
    }
    if (pX < minX || pX > maxX)
      return SpatialRelation.DISJOINT;
    return SpatialRelation.CONTAINS;
  }

  public SpatialRelation relate(Rectangle rect) {
    SpatialRelation yIntersect = relateYRange(rect.getMinY(), rect.getMaxY());
    if (yIntersect == SpatialRelation.DISJOINT)
      return SpatialRelation.DISJOINT;

    SpatialRelation xIntersect = relateXRange(rect.getMinX(), rect.getMaxX());
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

  //TODO might this utility move to SpatialRelation ?
  private static SpatialRelation relate_range(double int_min, double int_max, double ext_min, double ext_max) {
    if (ext_min > int_max || ext_max < int_min) {
      return SpatialRelation.DISJOINT;
    }

    if (ext_min >= int_min && ext_max <= int_max) {
      return SpatialRelation.CONTAINS;
    }

    if (ext_min <= int_min && ext_max >= int_max) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public SpatialRelation relateYRange(double ext_minY, double ext_maxY) {
    return relate_range(minY, maxY, ext_minY, ext_maxY);
  }

  @Override
  public SpatialRelation relateXRange(double ext_minX, double ext_maxX) {
    //For ext & this we have local minX and maxX variable pairs. We rotate them so that minX <= maxX
    double minX = this.minX;
    double maxX = this.maxX;
    if (ctx.isGeo()) {
      //unwrap dateline, plus do world-wrap short circuit
      double rawWidth = maxX - minX;
      if (rawWidth == 360)
        return SpatialRelation.CONTAINS;
      if (rawWidth < 0) {
        maxX = minX + (rawWidth + 360);
      }
      double ext_rawWidth = ext_maxX - ext_minX;
      if (ext_rawWidth == 360)
        return SpatialRelation.WITHIN;
      if (ext_rawWidth < 0) {
        ext_maxX = ext_minX + (ext_rawWidth + 360);
      }
      //shift to potentially overlap
      if (maxX < ext_minX) {
        minX += 360;
        maxX += 360;
      } else if (ext_maxX < minX) {
        ext_minX += 360;
        ext_maxX += 360;
      }
    }

    return relate_range(minX, maxX, ext_minX, ext_maxX);
  }

  @Override
  public String toString() {
    return "Rect(minX=" + minX + ",maxX=" + maxX + ",minY=" + minY + ",maxY=" + maxY + ")";
  }

  @Override
  public Point getCenter() {
    if (Double.isNaN(minX))
      return ctx.makePoint(Double.NaN, Double.NaN);
    final double y = getHeight() / 2 + minY;
    double x = getWidth() / 2 + minX;
    if (minX > maxX)//WGS84
      x = DistanceUtils.normLonDEG(x);//in case falls outside the standard range
    return new PointImpl(x, y, ctx);
  }

  @Override
  public boolean equals(Object obj) {
    return equals(this,obj);
  }

  /**
   * All {@link Rectangle} implementations should use this definition of {@link Object#equals(Object)}.
   */
  public static boolean equals(Rectangle thiz, Object o) {
    assert thiz != null;
    if (thiz == o) return true;
    if (!(o instanceof Rectangle)) return false;

    RectangleImpl rectangle = (RectangleImpl) o;

    if (Double.compare(rectangle.getMaxX(), thiz.getMaxX()) != 0) return false;
    if (Double.compare(rectangle.getMaxY(), thiz.getMaxY()) != 0) return false;
    if (Double.compare(rectangle.getMinX(), thiz.getMinX()) != 0) return false;
    if (Double.compare(rectangle.getMinY(), thiz.getMinY()) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return hashCode(this);
  }

  /**
   * All {@link Rectangle} implementations should use this definition of {@link Object#hashCode()}.
   */
  public static int hashCode(Rectangle thiz) {
    int result;
    long temp;
    temp = thiz.getMinX() != +0.0d ? Double.doubleToLongBits(thiz.getMinX()) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = thiz.getMaxX() != +0.0d ? Double.doubleToLongBits(thiz.getMaxX()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = thiz.getMinY() != +0.0d ? Double.doubleToLongBits(thiz.getMinY()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = thiz.getMaxY() != +0.0d ? Double.doubleToLongBits(thiz.getMaxY()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
