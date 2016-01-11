/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.context;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.io.BinaryCodec;
import com.spatial4j.core.io.LegacyShapeWriter;
import com.spatial4j.core.io.SupportedFormats;
import com.spatial4j.core.io.WKTReader;
import com.spatial4j.core.shape.*;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.spatial4j.core.shape.impl.ShapeFactoryImpl;

import java.text.ParseException;
import java.util.List;

/**
 * This is a facade to most of Spatial4j, holding things like {@link DistanceCalculator},
 * {@link ShapeFactoryImpl},
 * {@link com.spatial4j.core.io.ShapeIO}.
 * <p/>
 * If you want a typical geodetic context, just reference {@link #GEO}.  Otherwise,
 * You should either create and configure a {@link SpatialContextFactory} and then call
 * {@link SpatialContextFactory#newSpatialContext()}, OR, call
 * {@link com.spatial4j.core.context.SpatialContextFactory#makeSpatialContext(java.util.Map, ClassLoader)}
 * to do this via configuration data.
 * <p/>
 * Thread-safe & immutable.
 */
public class SpatialContext {

  /** A popular default SpatialContext implementation for geospatial. */
  public static final SpatialContext GEO = new SpatialContext(new SpatialContextFactory());

  //These are non-null
  private final boolean geo;
  private final ShapeFactory shapeFactory;
  private final DistanceCalculator calculator;
  private final Rectangle worldBounds;
  private final BinaryCodec binaryCodec;
  private final SupportedFormats formats;

  /**
   * Consider using {@link com.spatial4j.core.context.SpatialContextFactory} instead.
   *
   * @param geo Establishes geo vs cartesian / Euclidean.
   * @param calculator Optional; defaults to haversine or cartesian depending on {@code geo}.
   * @param worldBounds Optional; defaults to GEO_WORLDBOUNDS or MAX_WORLDBOUNDS depending on units.
   */
  @Deprecated
  public SpatialContext(boolean geo, DistanceCalculator calculator, Rectangle worldBounds) {
    this(initFromLegacyConstructor(geo, calculator, worldBounds));
  }

  private static SpatialContextFactory initFromLegacyConstructor(boolean geo,
                                                                 DistanceCalculator calculator,
                                                                 Rectangle worldBounds) {
    SpatialContextFactory factory = new SpatialContextFactory();
    factory.geo = geo;
    factory.distCalc = calculator;
    factory.worldBounds = worldBounds;
    return factory;
  }

  @Deprecated
  public SpatialContext(boolean geo) {
    this(initFromLegacyConstructor(geo, null, null));
  }

  /**
   * Called by {@link com.spatial4j.core.context.SpatialContextFactory#newSpatialContext()}.
   */
  public SpatialContext(SpatialContextFactory factory) {
    this.geo = factory.geo;

    this.shapeFactory = factory.makeShapeFactory(this);

    if (factory.distCalc == null) {
      this.calculator = isGeo()
              ? new GeodesicSphereDistCalc.Haversine()
              : new CartesianDistCalc();
    } else {
      this.calculator = factory.distCalc;
    }

    //TODO remove worldBounds from Spatial4j: see Issue #55
    Rectangle bounds = factory.worldBounds;
    if (bounds == null) {
      this.worldBounds = isGeo()
              ? new RectangleImpl(-180, 180, -90, 90, this)
              : new RectangleImpl(-Double.MAX_VALUE, Double.MAX_VALUE,
              -Double.MAX_VALUE, Double.MAX_VALUE, this);
    } else {
      if (isGeo() && !bounds.equals(new RectangleImpl(-180, 180, -90, 90, this)))
        throw new IllegalArgumentException("for geo (lat/lon), bounds must be " + GEO.getWorldBounds());
      if (bounds.getMinX() > bounds.getMaxX())
        throw new IllegalArgumentException("worldBounds minX should be <= maxX: "+ bounds);
      if (bounds.getMinY() > bounds.getMaxY())
        throw new IllegalArgumentException("worldBounds minY should be <= maxY: "+ bounds);
      //hopefully worldBounds' rect implementation is compatible
      this.worldBounds = new RectangleImpl(bounds, this);
    }

    this.binaryCodec = factory.makeBinaryCodec(this);
    
    factory.checkDefaultFormats();
    this.formats = factory.makeFormats(this);
  }

  /** A factory for {@link Shape}s. */
  public ShapeFactory getShapeFactory() {
    return shapeFactory;
  }

  public SupportedFormats getFormats() {
    return formats;
  }

  public DistanceCalculator getDistCalc() {
    return calculator;
  }

  /** Convenience that uses {@link #getDistCalc()} */
  public double calcDistance(Point p, double x2, double y2) {
    return getDistCalc().distance(p, x2, y2);
  }

  /** Convenience that uses {@link #getDistCalc()} */
  public double calcDistance(Point p, Point p2) {
    return getDistCalc().distance(p, p2);
  }

  /**
   * The extent of x & y coordinates should fit within the return'ed rectangle.
   * Do *NOT* invoke reset() on this return type.
   */
  public Rectangle getWorldBounds() {
    return worldBounds;
  }

  /** If true then {@link #normX(double)} will wrap longitudes outside of the standard
   * geodetic boundary into it. Example: 181 will become -179. */
  public boolean isNormWrapLongitude() {
    return shapeFactory.isNormWrapLongitude();
  }

  /** Is the mathematical world model based on a sphere, or is it a flat plane? The word
   * "geodetic" or "geodesic" is sometimes used to refer to the former, and the latter is sometimes
   * referred to as "Euclidean" or "cartesian". */
  public boolean isGeo() {
    return geo;
  }

  /** Normalize the 'x' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link com.spatial4j.core.io.WKTReader} before creating a shape. */
  public double normX(double x) {
    return shapeFactory.normX(x);
  }

  /** Normalize the 'y' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link com.spatial4j.core.io.WKTReader} before creating a shape. */
  public double normY(double y) { return shapeFactory.normY(y); }

  /** Ensure fits in {@link #getWorldBounds()}. It's called by any shape factory method that
   * gets an 'x' dimension. */
  public void verifyX(double x) {
    shapeFactory.verifyX(x);
  }

  /** Ensure fits in {@link #getWorldBounds()}. It's called by any shape factory method that
   * gets a 'y' dimension. */
  public void verifyY(double y) {
    shapeFactory.verifyY(y);
  }

  /** Construct a point. */
  public Point makePoint(double x, double y) {
    return shapeFactory.makePoint(x, y);
  }

  /** Construct a rectangle. */
  public Rectangle makeRectangle(Point lowerLeft, Point upperRight) {
    return shapeFactory.makeRectangle(lowerLeft, upperRight);
  }

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180)
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline.
   */
  public Rectangle makeRectangle(double minX, double maxX, double minY, double maxY) {
    return shapeFactory.makeRectangle(minX, maxX, minY, maxY);
  }

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  public Circle makeCircle(double x, double y, double distance) {
    return shapeFactory.makeCircle(x, y, distance);
  }

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  public Circle makeCircle(Point point, double distance) {
    return shapeFactory.makeCircle(point, distance);
  }

  /** Constructs a line string. It's an ordered sequence of connected vertexes. There
   * is no official shape/interface for it yet so we just return Shape. */
  public Shape makeLineString(List<Point> points) {
    return shapeFactory.makeLineString(points);
  }

  /** Constructs a buffered line string. It's an ordered sequence of connected vertexes,
   * with a buffer distance along the line in all directions. There
   * is no official shape/interface for it so we just return Shape. */
  public Shape makeBufferedLineString(List<Point> points, double buf) {
    return shapeFactory.makeBufferedLineString(points, buf);
  }

  /** Construct a ShapeCollection, analogous to an OGC GeometryCollection. */
  public <S extends Shape> ShapeCollection<S> makeCollection(List<S> coll) {
    return shapeFactory.makeCollection(coll);
  }

  /** The {@link com.spatial4j.core.io.WKTReader} used by {@link #readShapeFromWkt(String)}. */
  @Deprecated
  public WKTReader getWktShapeParser() {
    return (WKTReader)formats.getWktReader();
  }

  /** Reads a shape from the string formatted in WKT.
   * @see com.spatial4j.core.io.WKTReader
   * @param wkt non-null WKT.
   * @return non-null
   * @throws ParseException if it failed to parse.
   */
  @Deprecated
  public Shape readShapeFromWkt(String wkt) throws ParseException, InvalidShapeException {
    return getWktShapeParser().parse(wkt);
  }

  public BinaryCodec getBinaryCodec() { return binaryCodec; }

  /**
   * Try to read a shape from any supported formats
   * 
   * @param value
   * @return shape or null if unable to parse any shape
   * @throws InvalidShapeException
   */
  @Deprecated
  public Shape readShape(String value) throws InvalidShapeException {
    return formats.read(value);
  }

  /** Writes the shape to a String using the old/deprecated
   * {@link com.spatial4j.core.io.LegacyShapeWriter}. The JTS based subclass will write it
   * to WKT if the legacy format doesn't support that shape.
   * <b>Spatial4j in the near future won't support writing shapes to strings.</b>
   * @param shape non-null
   * @return non-null
   */
  @Deprecated
  public String toString(Shape shape) {
    return LegacyShapeWriter.writeShape(shape);
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
