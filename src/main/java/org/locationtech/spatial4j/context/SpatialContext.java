/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context;

import org.locationtech.spatial4j.distance.CartesianDistCalc;
import org.locationtech.spatial4j.distance.DistanceCalculator;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.BinaryCodec;
import org.locationtech.spatial4j.io.LegacyShapeWriter;
import org.locationtech.spatial4j.io.SupportedFormats;
import org.locationtech.spatial4j.io.WKTReader;
import org.locationtech.spatial4j.shape.*;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;

import java.text.ParseException;
import java.util.List;

/**
 * This is a facade to most of Spatial4j, holding things like {@link DistanceCalculator},
 * {@link ShapeFactory},
 * {@link org.locationtech.spatial4j.io.ShapeIO}.
 * <p>
 * If you want a typical geodetic context, just reference {@link #GEO}.  Otherwise,
 * You should either create and configure a {@link SpatialContextFactory} and then call
 * {@link SpatialContextFactory#newSpatialContext()}, OR, call
 * {@link org.locationtech.spatial4j.context.SpatialContextFactory#makeSpatialContext(java.util.Map, ClassLoader)}
 * to do this via configuration data.
 * <p>
 * Thread-safe &amp; immutable.
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
   * Consider using {@link org.locationtech.spatial4j.context.SpatialContextFactory} instead.
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
   * Called by {@link org.locationtech.spatial4j.context.SpatialContextFactory#newSpatialContext()}.
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
   * The extent of x &amp; y coordinates should fit within the return'ed rectangle.
   * Do *NOT* invoke reset() on this return type.
   */
  public Rectangle getWorldBounds() {
    return worldBounds;
  }

  /** If true then {@link #normX(double)} will wrap longitudes outside of the standard
   * geodetic boundary into it. Example: 181 will become -179. */
  @Deprecated
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
   * is called by {@link org.locationtech.spatial4j.io.WKTReader} before creating a shape. */
  @Deprecated
  public double normX(double x) {
    return shapeFactory.normX(x);
  }

  /** Normalize the 'y' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link org.locationtech.spatial4j.io.WKTReader} before creating a shape. */
  @Deprecated
  public double normY(double y) { return shapeFactory.normY(y); }

  /** Ensure fits in {@link #getWorldBounds()}. It's called by any shape factory method that
   * gets an 'x' dimension. */
  @Deprecated
  public void verifyX(double x) {
    shapeFactory.verifyX(x);
  }

  /** Ensure fits in {@link #getWorldBounds()}. It's called by any shape factory method that
   * gets a 'y' dimension. */
  @Deprecated
  public void verifyY(double y) {
    shapeFactory.verifyY(y);
  }

  /** Construct a point. */
  @Deprecated
  public Point makePoint(double x, double y) {
    return shapeFactory.pointXY(x, y);
  }

  /** Construct a rectangle. */
  @Deprecated
  public Rectangle makeRectangle(Point lowerLeft, Point upperRight) {
    return shapeFactory.rect(lowerLeft, upperRight);
  }

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180) (aka anti-meridian)
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline.
   */
  @Deprecated
  public Rectangle makeRectangle(double minX, double maxX, double minY, double maxY) {
    return shapeFactory.rect(minX, maxX, minY, maxY);
  }

  /** Construct a circle. The units of "distance" should be the same as x &amp; y. */
  @Deprecated
  public Circle makeCircle(double x, double y, double distance) {
    return shapeFactory.circle(x, y, distance);
  }

  /** Construct a circle. The units of "distance" should be the same as x &amp; y. */
  @Deprecated
  public Circle makeCircle(Point point, double distance) {
    return shapeFactory.circle(point, distance);
  }

  /** Constructs a line string. It's an ordered sequence of connected vertexes. There
   * is no official shape/interface for it yet so we just return Shape. */
  @Deprecated
  public Shape makeLineString(List<Point> points) {
    return shapeFactory.lineString(points, 0);
  }

  /** Constructs a buffered line string. It's an ordered sequence of connected vertexes,
   * with a buffer distance along the line in all directions. There
   * is no official shape/interface for it so we just return Shape. */
  @Deprecated
  public Shape makeBufferedLineString(List<Point> points, double buf) {
    return shapeFactory.lineString(points, buf);
  }

  /** Construct a ShapeCollection, analogous to an OGC GeometryCollection. */
  @Deprecated
  public <S extends Shape> ShapeCollection<S> makeCollection(List<S> coll) {
    return shapeFactory.multiShape(coll);
  }

  /** The {@link org.locationtech.spatial4j.io.WKTReader} used by {@link #readShapeFromWkt(String)}. */
  @Deprecated
  public WKTReader getWktShapeParser() {
    return (WKTReader)formats.getWktReader();
  }

  /** Reads a shape from the string formatted in WKT.
   * @see org.locationtech.spatial4j.io.WKTReader
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
   * {@link org.locationtech.spatial4j.io.LegacyShapeWriter}. The JTS based subclass will write it
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
