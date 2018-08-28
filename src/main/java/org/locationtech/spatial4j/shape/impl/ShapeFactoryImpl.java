/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.impl;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.*;

import java.util.ArrayList;
import java.util.List;

/** The default {@link org.locationtech.spatial4j.shape.ShapeFactory}.  It does not support polygon shapes. */
public class ShapeFactoryImpl implements ShapeFactory {

  protected final SpatialContext ctx;

  private final boolean normWrapLongitude;

  public ShapeFactoryImpl(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
    this.normWrapLongitude = ctx.isGeo() && factory.normWrapLongitude;
  }

  @Override
  public SpatialContext getSpatialContext() {
    return ctx;
  }

  @Override
  public boolean isNormWrapLongitude() {
    return normWrapLongitude;
  }

  @Override
  public double normX(double x) {
    if (normWrapLongitude)
      x = DistanceUtils.normLonDEG(x);
    return x;
  }

  @Override
  public double normY(double y) { return y; }

  @Override
  public double normZ(double z) { return z; }

  @Override
  public double normDist(double d) {
    return d;
  }

  @Override
  public void verifyX(double x) {
    Rectangle bounds = ctx.getWorldBounds();
    if (x < bounds.getMinX() || x > bounds.getMaxX())//NaN will pass
      throw new InvalidShapeException("Bad X value "+x+" is not in boundary "+bounds);
  }

  @Override
  public void verifyY(double y) {
    Rectangle bounds = ctx.getWorldBounds();
    if (y < bounds.getMinY() || y > bounds.getMaxY())//NaN will pass
      throw new InvalidShapeException("Bad Y value "+y+" is not in boundary "+bounds);
  }

  @Override
  public void verifyZ(double z) { // bounds has no 'z' for this simple shapeFactory
  }

  @Override
  public Point pointXY(double x, double y) {
    verifyX(x);
    verifyY(y);
    return new PointImpl(x, y, ctx);
  }

  @Override
  public Point pointLatLon(double latitude, double longitude) {
    verifyX(longitude);
    verifyY(latitude);
    return new PointImpl(longitude, latitude, ctx);
  }

  @Override
  public Point pointXYZ(double x, double y, double z) {
    return pointXY(x, y); // or throw?
  }

  @Override
  public Rectangle rect(Point lowerLeft, Point upperRight) {
    return rect(lowerLeft.getX(), upperRight.getX(),
            lowerLeft.getY(), upperRight.getY());
  }

  @Override
  public Rectangle rect(double minX, double maxX, double minY, double maxY) {
    Rectangle bounds = ctx.getWorldBounds();
    // Y
    if (minY < bounds.getMinY() || maxY > bounds.getMaxY())//NaN will pass
      throw new InvalidShapeException("Y values ["+minY+" to "+maxY+"] not in boundary "+bounds);
    if (minY > maxY)
      throw new InvalidShapeException("maxY must be >= minY: " + minY + " to " + maxY);
    // X
    if (ctx.isGeo()) {
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
      if (minX < bounds.getMinX() || maxX > bounds.getMaxX())//NaN will pass
        throw new InvalidShapeException("X values ["+minX+" to "+maxX+"] not in boundary "+bounds);
      if (minX > maxX)
        throw new InvalidShapeException("maxX must be >= minX: " + minX + " to " + maxX);
    }
    return new RectangleImpl(minX, maxX, minY, maxY, ctx);
  }

  @Override
  public Circle circle(double x, double y, double distance) {
    return circle(pointXY(x, y), distance);
  }

  @Override
  public Circle circle(Point point, double distance) {
    if (distance < 0)
      throw new InvalidShapeException("distance must be >= 0; got " + distance);
    if (ctx.isGeo()) {
      if (distance > 180) {
        // (it's debatable whether to error or not)
        //throw new InvalidShapeException("distance must be <= 180; got " + distance);
        distance = 180;
      }
      return new GeoCircle(point, distance, ctx);
    } else {
      return new CircleImpl(point, distance, ctx);
    }
  }

  @Override
  public Shape lineString(List<Point> points, double buf) {
    return new BufferedLineString(points, buf, ctx.isGeo(), ctx);
  }

  @Override
  public LineStringBuilder lineString() {
    return new LineStringBuilder() {
      final List<Point> points = new ArrayList<>();
      double bufferDistance = 0;

      @Override
      public LineStringBuilder buffer(double distance) {
        this.bufferDistance = distance;
        return this;
      }

      @Override
      public LineStringBuilder pointXY(double x, double y) {
        points.add(ShapeFactoryImpl.this.pointXY(x, y));
        return this;
      }

      @Override
      public LineStringBuilder pointXYZ(double x, double y, double z) {
        points.add(ShapeFactoryImpl.this.pointXYZ(x, y, z));
        return this;
      }

      @Override
      public LineStringBuilder pointLatLon(double latitude, double longitude) {
        points.add(ShapeFactoryImpl.this.pointLatLon(latitude, longitude));
        return this;
      }

      @Override
      public Shape build() {
        return new BufferedLineString(points, bufferDistance, false, ctx);
      }
    };
  }

  @Override
  public <S extends Shape> ShapeCollection<S> multiShape(List<S> coll) {
    return new ShapeCollection<>(coll, ctx);
  }

  @Override
  public <T extends Shape> MultiShapeBuilder<T> multiShape(Class<T> shapeClass) {
    return new GeneralShapeMultiShapeBuilder<>();
  }

  @Override
  public MultiPointBuilder multiPoint() {
    return new GeneralShapeMultiShapeBuilder<>();
  }

  @Override
  public MultiLineStringBuilder multiLineString() {
    return new GeneralShapeMultiShapeBuilder<>();
  }

  @Override
  public MultiPolygonBuilder multiPolygon() {
    return new GeneralShapeMultiShapeBuilder<>();
  }

  @Override
  public PolygonBuilder polygon() {
    throw new UnsupportedOperationException("Unsupported shape of this SpatialContext. Try JTS or Geo3D.");
  }

  protected class GeneralShapeMultiShapeBuilder<T extends Shape> implements MultiShapeBuilder<T>,
      MultiPointBuilder, MultiLineStringBuilder, MultiPolygonBuilder {
    protected List<Shape> shapes = new ArrayList<>();

    @Override
    public MultiShapeBuilder<T> add(T shape) {
      shapes.add(shape);
      return this;
    }

    @Override
    public MultiPointBuilder pointXY(double x, double y) {
      shapes.add(ShapeFactoryImpl.this.pointXY(x, y));
      return this;
    }

    @Override
    public MultiPointBuilder pointXYZ(double x, double y, double z) {
      shapes.add(ShapeFactoryImpl.this.pointXYZ(x, y, z));
      return this;
    }

    @Override
    public MultiPointBuilder pointLatLon(double latitude, double longitude) {
      shapes.add(ShapeFactoryImpl.this.pointLatLon(latitude, longitude));
      return this;
    }

    @Override
    public LineStringBuilder lineString() {
      return ShapeFactoryImpl.this.lineString();
    }

    @Override
    public MultiLineStringBuilder add(LineStringBuilder lineStringBuilder) {
      shapes.add(lineStringBuilder.build());
      return this;
    }

    @Override
    public PolygonBuilder polygon() {
      return ShapeFactoryImpl.this.polygon();
    }

    @Override
    public MultiPolygonBuilder add(PolygonBuilder polygonBuilder) {
      shapes.add(polygonBuilder.build());
      return this;
    }

    @Override
    public Shape build() {
      return new ShapeCollection<>(shapes, ctx);
    }
  }
}
