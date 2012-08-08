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

package com.spatial4j.core.context;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.io.ShapeReadWriter;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.CircleImpl;
import com.spatial4j.core.shape.impl.GeoCircle;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;

/**
 * This is a facade to most of Spatial4j, holding things like {@link
 * DistanceCalculator}, and the coordinate world boundaries, and acting as a
 * factory for the {@link Shape}s.
 * <p/>
 * A SpatialContext has public constructors, but note the convenience instance
 * {@link #GEO}.  Also, if you wish to construct one based on configuration
 * information then consider using {@link SpatialContextFactory}.
 * <p/>
 * Thread-safe & immutable.
 */
public class SpatialContext {

  public static final RectangleImpl GEO_WORLDBOUNDS = new RectangleImpl(-180,180,-90,90);
  public static final RectangleImpl MAX_WORLDBOUNDS;
  static {
    double v = Double.MAX_VALUE;
    MAX_WORLDBOUNDS = new RectangleImpl(-v, v, -v, v);
  }

  /** A popular default SpatialContext implementation for geospatial. */
  public static final SpatialContext GEO = new SpatialContext(true);
  //note: any static convenience instances must be declared after the world bounds

  //These are non-null
  private final boolean geo;
  private final DistanceCalculator calculator;
  private final Rectangle worldBounds;

  private final ShapeReadWriter shapeReadWriter;

  /**
   * @param geo Establishes geo vs cartesian / Euclidean.
   * @param calculator Optional; defaults to Haversine or cartesian depending on units.
   * @param worldBounds Optional; defaults to GEO_WORLDBOUNDS or MAX_WORLDBOUNDS depending on units.
   */
  public SpatialContext(boolean geo, DistanceCalculator calculator, Rectangle worldBounds) {
    this.geo = geo;

    if (calculator == null) {
      calculator = isGeo()
          ? new GeodesicSphereDistCalc.Haversine()
          : new CartesianDistCalc();
    }
    this.calculator = calculator;

    if (worldBounds == null) {
      worldBounds = isGeo() ? GEO_WORLDBOUNDS : MAX_WORLDBOUNDS;
    } else {
      if (isGeo())
        assert new RectangleImpl(worldBounds).equals(GEO_WORLDBOUNDS);
      if (worldBounds.getCrossesDateLine())
        throw new IllegalArgumentException("worldBounds shouldn't cross dateline: "+worldBounds);
    }
    //copy so we can ensure we have the right implementation
    worldBounds = makeRect(worldBounds.getMinX(),worldBounds.getMaxX(),worldBounds.getMinY(),worldBounds.getMaxY());
    this.worldBounds = worldBounds;

    shapeReadWriter = makeShapeReadWriter();
  }

  public SpatialContext(boolean geo) {
    this(geo, null, null);
  }

  protected ShapeReadWriter makeShapeReadWriter() {
    return new ShapeReadWriter(this);
  }

  public DistanceCalculator getDistCalc() {
    return calculator;
  }

  public Rectangle getWorldBounds() {
    return worldBounds;
  }

  /** If {@link #isGeo()} then calls {@link DistanceUtils#normLonDEG(double)}. */
  public double normX(double x) {
    if (isGeo()) {
      return DistanceUtils.normLonDEG(x);
    } else {
      return x;
    }
  }

  /** If {@link #isGeo()} then calls {@link DistanceUtils#normLatDEG(double)}. */
  public double normY(double y) {
    if (isGeo()) {
      y = DistanceUtils.normLatDEG(y);
    }
    return y;
  }

  /** Is this a geospatial context (true) or simply 2d spatial (false). */
  public boolean isGeo() {
    return geo;
  }

  /** Construct a point. The parameters will be normalized. */
  public Point makePoint(double x, double y) {
    return new PointImpl(normX(x),normY(y));
  }

  /** Construct a rectangle. The parameters will be normalized. */
  public Rectangle makeRect(Point lowerLeft, Point upperRight) {
    return makeRect(lowerLeft.getX(), upperRight.getX(),
        lowerLeft.getY(), upperRight.getY());
  }

  /** Construct a rectangle. The parameters will be normalized. */
  public Rectangle makeRect(double minX, double maxX, double minY, double maxY) {
    //--Normalize parameters
    if (isGeo()) {
      double delta = calcWidth(minX,maxX);
      if (delta >= 360) {
        //The only way to officially support complete longitude wrap-around is via western longitude = -180. We can't
        // support any point because 0 is undifferentiated in sign.
        minX = -180;
        maxX = 180;
      } else {
        minX = normX(minX);
        maxX = normX(maxX);
        assert Math.abs(delta - calcWidth(minX,maxX)) < 0.0001;//recompute delta; should be the same
        //If an edge coincides with the dateline then don't make this rect cross it
        if (delta > 0) {
          if (minX == 180) {
            minX = -180;
            maxX = -180 + delta;
          } else if (maxX == -180) {
            maxX = 180;
            minX = 180 - delta;
          }
        }
      }
      if (minY > maxY) {
        throw new IllegalArgumentException("maxY must be >= minY: "+minY+" to "+maxY);
      }
      if (minY < -90 || minY > 90 || maxY < -90 || maxY > 90)
        throw new IllegalArgumentException(
                "minY or maxY is outside of -90 to 90 bounds. What did you mean?: "+minY+" to "+maxY);
//        debatable what to do in this situation.
//        if (minY < -90) {
//          minX = -180;
//          maxX = 180;
//          maxY = Math.min(90,Math.max(maxY,-90 + (-90 - minY)));
//          minY = -90;
//        }
//        if (maxY > 90) {
//          minX = -180;
//          maxX = 180;
//          minY = Math.max(-90,Math.min(minY,90 - (maxY - 90)));
//          maxY = 90;
//        }

    } else {
      //these normalizations probably won't do anything since it's not geo but should probably call them any way.
      minX = normX(minX);
      maxX = normX(maxX);
      minY = normY(minY);
      maxY = normY(maxY);
    }
    return new RectangleImpl(minX, maxX, minY, maxY);
  }

  private double calcWidth(double minX, double maxX) {
    double w = maxX - minX;
    if (w < 0) {//only true when minX > maxX (WGS84 assumed)
      w += 360;
      assert w >= 0;
    }
    return w;
  }

  /**
   * Construct a circle. The parameters will be normalized. The units of
   * "distance" should be the same as x & y.
   */
  public Circle makeCircle(double x, double y, double distance) {
    return makeCircle(makePoint(x, y), distance);
  }

  /**
   * Construct a circle. The parameters will be normalized. The units of
   * "distance" should be the same as x & y.
   */
  public Circle makeCircle(Point point, double distance) {
    if (distance < 0)
      throw new InvalidShapeException("distance must be >= 0; got " + distance);
    if (isGeo())
      return new GeoCircle(point, Math.min(distance, 180), this);
    else
      return new CircleImpl(point, distance, this);
  }

  @Deprecated
  public Shape readShape(String value) throws InvalidShapeException {
    return shapeReadWriter.readShape(value);
  }

  @Deprecated
  public String toString(Shape shape) {
    return shapeReadWriter.writeShape(shape);
  }

  @Override
  public String toString() {
    if (this.equals(GEO)) {
      return GEO.getClass().getSimpleName()+".GEO";
    } else {
      return getClass().getSimpleName()+"{" +
          "geo=" + geo +
          ", calculator=" + calculator +
          ", worldBounds=" + worldBounds +
          '}';
    }
  }

}
