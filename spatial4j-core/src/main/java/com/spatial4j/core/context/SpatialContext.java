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

  /** A popular default SpatialContext implementation for geospatial. */
  public static final SpatialContext GEO = new SpatialContext(true);

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
      worldBounds = isGeo()
              ? new RectangleImpl(-180, 180, -90, 90, this)
              : new RectangleImpl(-Double.MAX_VALUE, Double.MAX_VALUE,
                  -Double.MAX_VALUE, Double.MAX_VALUE, this);
    } else {
      if (isGeo())
        assert worldBounds.equals(new RectangleImpl(-180, 180, -90, 90, this));
      if (worldBounds.getCrossesDateLine())
        throw new IllegalArgumentException("worldBounds shouldn't cross dateline: "+worldBounds);
    }
    //hopefully worldBounds' rect implementation is compatible
    this.worldBounds = new RectangleImpl(worldBounds, this);

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

  /**
   * The extent of x & y coordinates should fit within the return'ed rectangle.
   * Do *NOT* invoke reset() on this return type.
   */
  public Rectangle getWorldBounds() {
    return worldBounds;
  }

  /** Is this a geospatial context (true) or simply 2d spatial (false). */
  public boolean isGeo() {
    return geo;
  }

  /** Ensure fits in {@link #getWorldBounds()} */
  public void verifyX(double x) {
    Rectangle bounds = getWorldBounds();
    if (!(x >= bounds.getMinX() && x <= bounds.getMaxX()))//NaN will fail
      throw new InvalidShapeException("Bad X value "+x+" is not in boundary "+bounds);
  }

  /** Ensure fits in {@link #getWorldBounds()} */
  public void verifyY(double y) {
    Rectangle bounds = getWorldBounds();
    if (!(y >= bounds.getMinY() && y <= bounds.getMaxY()))//NaN will fail
      throw new InvalidShapeException("Bad Y value "+y+" is not in boundary "+bounds);
  }

  /** Construct a point. */
  public Point makePoint(double x, double y) {
    verifyX(x);
    verifyY(y);
    return new PointImpl(x, y, this);
  }

  /** Construct a rectangle. */
  public Rectangle makeRectangle(Point lowerLeft, Point upperRight) {
    return makeRectangle(lowerLeft.getX(), upperRight.getX(),
            lowerLeft.getY(), upperRight.getY());
  }

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180)
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline.
   */
  public Rectangle makeRectangle(double minX, double maxX, double minY, double maxY) {
    Rectangle bounds = getWorldBounds();
    // Y
    if (!(minY >= bounds.getMinY() && maxY <= bounds.getMaxY()))//NaN will fail
      throw new InvalidShapeException("Y values ["+minY+" to "+maxY+"] not in boundary "+bounds);
    if (minY > maxY)
      throw new InvalidShapeException("maxY must be >= minY: " + minY + " to " + maxY);
    // X
    if (isGeo()) {
      verifyX(minX);
      verifyX(maxX);
      //TODO consider removing this logic so that there is no normalization here
      //if (minX != maxX) {   USUALLY TRUE, inline check below
      //If an edge coincides with the dateline then don't make this rect cross it
      if (minX == 180 && minX != maxX) {
        minX = -180;
      } else if (maxX == -180 && minX != maxX) {
        maxX = 180;
      }
      //}
    } else {
      if (!(minX >= bounds.getMinX() && maxX <= bounds.getMaxX()))//NaN will fail
        throw new InvalidShapeException("X values ["+minX+" to "+maxX+"] not in boundary "+bounds);
      if (minX > maxX)
        throw new InvalidShapeException("maxX must be >= minX: " + minX + " to " + maxX);
    }
    return new RectangleImpl(minX, maxX, minY, maxY, this);
  }

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  public Circle makeCircle(double x, double y, double distance) {
    return makeCircle(makePoint(x, y), distance);
  }

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  public Circle makeCircle(Point point, double distance) {
    if (distance < 0)
      throw new InvalidShapeException("distance must be >= 0; got " + distance);
    if (isGeo()) {
      if (distance > 180)
        throw new InvalidShapeException("distance must be <= 180; got " + distance);
      return new GeoCircle(point, distance, this);
    } else {
      return new CircleImpl(point, distance, this);
    }
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
