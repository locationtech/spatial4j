/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.*;

import java.util.List;

/** The default {@link com.spatial4j.core.shape.ShapeFactory}. */
public class ShapeFactoryImpl implements ShapeFactory {

  protected final SpatialContext ctx;

  private final boolean normWrapLongitude;

  public ShapeFactoryImpl(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
    this.normWrapLongitude = ctx.isGeo() && factory.normWrapLongitude;
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
  public Point makePoint(double x, double y) {
    verifyX(x);
    verifyY(y);
    return new PointImpl(x, y, ctx);
  }

  @Override
  public Rectangle makeRectangle(Point lowerLeft, Point upperRight) {
    return makeRectangle(lowerLeft.getX(), upperRight.getX(),
            lowerLeft.getY(), upperRight.getY());
  }

  @Override
  public Rectangle makeRectangle(double minX, double maxX, double minY, double maxY) {
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
  public Circle makeCircle(double x, double y, double distance) {
    return makeCircle(makePoint(x, y), distance);
  }

  @Override
  public Circle makeCircle(Point point, double distance) {
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
  public Shape makeLineString(List<Point> points) {
    return new BufferedLineString(points, 0, false, ctx);
  }

  @Override
  public Shape makeBufferedLineString(List<Point> points, double buf) {
    return new BufferedLineString(points, buf, ctx.isGeo(), ctx);
  }

  @Override
  public <S extends Shape> ShapeCollection<S> makeCollection(List<S> coll) {
    return new ShapeCollection<S>(coll, ctx);
  }

}
