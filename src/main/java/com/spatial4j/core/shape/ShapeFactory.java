/*******************************************************************************
 * Copyright (c) 2016 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.shape;

import java.util.List;

/** A factory for {@link Shape}s. */
public interface ShapeFactory {

  /** If true then {@link #normX(double)} will wrap longitudes outside of the standard
   * geodetic boundary into it. Example: 181 will become -179. */
  boolean isNormWrapLongitude();

  /** Normalize the 'x' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link com.spatial4j.core.io.WKTReader} before creating a shape. */
  double normX(double x);

  /** Normalize the 'y' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link com.spatial4j.core.io.WKTReader} before creating a shape. */
  double normY(double y);

  /** Ensure fits in the world bounds. It's called by any shape factory method that
   * gets an 'x' dimension. */
  void verifyX(double x);

  /** Ensure fits in the world bounds. It's called by any shape factory method that
   * gets a 'y' dimension. */
  void verifyY(double y);

  /** Construct a point. */
  Point makePoint(double x, double y);

  /** Construct a rectangle. */
  Rectangle makeRectangle(Point lowerLeft, Point upperRight);

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180)
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline.
   */
  Rectangle makeRectangle(double minX, double maxX, double minY, double maxY);

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  Circle makeCircle(double x, double y, double distance);

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  Circle makeCircle(Point point, double distance);

  /** Constructs a line string. It's an ordered sequence of connected vertexes. There
   * is no official shape/interface for it yet so we just return Shape. */
  Shape makeLineString(List<Point> points);

  /** Constructs a buffered line string. It's an ordered sequence of connected vertexes,
   * with a buffer distance along the line in all directions. There
   * is no official shape/interface for it so we just return Shape. */
  Shape makeBufferedLineString(List<Point> points, double buf);

  /** Construct a ShapeCollection, analogous to an OGC GeometryCollection. */
  <S extends Shape> ShapeCollection<S> makeCollection(List<S> coll);
}
