/*******************************************************************************
 * Copyright (c) 2016 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.shape;

import com.spatial4j.core.context.SpatialContext;

import java.util.List;

/**
 * A factory for {@link Shape}s.
 * Stateless and Threadsafe, except for any returned builders.
 */
public interface ShapeFactory {

  SpatialContext getSpatialContext();

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
  Point pointXY(double x, double y);

  /** Construct a point of 3 dimensions.  The implementation might ignore unsupported
   * dimensions or throw an error. */
  Point pointXYZ(double x, double y, double z);

  /** Construct a rectangle. */
  Rectangle rect(Point lowerLeft, Point upperRight);

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180)
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline.
   */
  Rectangle rect(double minX, double maxX, double minY, double maxY);

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  Circle circle(double x, double y, double distance);

  /** Construct a circle. The units of "distance" should be the same as x & y. */
  Circle circle(Point point, double distance);

  /** Constructs a line string with a possible buffer. It's an ordered sequence of connected vertexes,
   * with a buffer distance along the line in all directions. There
   * is no official shape/interface for it so we just return Shape. */
  @Deprecated
  Shape lineString(List<Point> points, double buf);

  /** Construct a ShapeCollection, analogous to an OGC GeometryCollection. */
  <S extends Shape> ShapeCollection<S> multiShape(List<S> coll);

  // BUILDERS:

  /** (Builder) Constructs a line string, with a possible buffer.
   * It's an ordered sequence of connected vertexes.
   * There is no official shape/interface for it yet so we just return Shape. */
  LineStringBuilder lineString();

  /** (Builder) Constructs a polygon.
   * There is no official shape/interface for it yet so we just return Shape. */
  PolygonBuilder polygon();

  /** (Builder) Constructs a Shape aggregate in which each component/member
   * is an instance of the specified class.
   */
  <T extends Shape> MultiShapeBuilder<T> multiShape(Class<T> shapeClass);

  MultiPointBuilder multiPoint();

  MultiLineStringBuilder multiLineString();

  MultiPolygonBuilder multiPolygon();

  // misc:
  //Shape buffer(Shape shape);  ?

  // TODO need Polygon shape
  // TODO need LineString shape
  // TODO need BufferedLineString shape
  // TODO need ShapeCollection to be typed

  interface PointsBuilder<T> {
    T pointXY(double x, double y);
    T pointXYZ(double x, double y, double z);
  }

  interface LineStringBuilder<T extends LineStringBuilder> extends PointsBuilder<T> {
    // TODO add dimensionality hint method?

    LineStringBuilder buffer(double distance);

    Shape build();
  }

  interface PolygonBuilder<T extends PolygonBuilder> extends PointsBuilder<T> {
    // TODO add dimensionality hint method?

    /** Starts a new hole. You must add at least 4 points; furthermore the first and last must be the same.
     * And don't forget to call {@link HoleBuilder#endHole()}! */
    HoleBuilder<?, T> hole();

    /** Builds the polygon and renders this builder instance invalid.
     */
    Shape build();// never a Rect

    Shape buildOrRect();

    interface HoleBuilder<T extends HoleBuilder, P extends PolygonBuilder> extends PointsBuilder<T> {
      /** Finishes the hole and returns the {@link PolygonBuilder}. Calling this is optional.*/
      P endHole();
    }
  }

  // TODO add dimensionality hint method to the multi* builders?

  interface MultiShapeBuilder<T extends Shape> {
    // TODO add dimensionality hint method?

    MultiShapeBuilder<T> add(T shape);

    //ShapeCollection<T> build(); TODO wait till it's a typed interface
    Shape build();
  }

  interface MultiPointBuilder extends PointsBuilder<MultiPointBuilder> {

    Shape build();  // TODO MultiShape<Point>
  }

  interface MultiLineStringBuilder {

    LineStringBuilder lineString();

    MultiLineStringBuilder add(LineStringBuilder lineStringBuilder);

    Shape build(); // TODO MultiShape<LineString>
  }

  interface MultiPolygonBuilder {

    PolygonBuilder polygon();

    MultiPolygonBuilder add(PolygonBuilder polygonBuilder);

    Shape build(); // TODO MultiShape<Polygon>
  }

  /*
    // TODO should normWrapLongitude, normX, normY, verifyX, verifyY be here too?
  // TODO use make* style?

  Point pointXY(double x, double y);
  Point pointXYZ(double x, double y, double z);

  Rectangle rect(Point lowerLeft, Point upperRight);
  Rectangle rect(double minX, double maxX, double minY, double maxY);

  Circle circle(double x, double y, double distance);
  Circle circle(Point point, double distance);

  LineStringBuilder lineString();
  LineStringBuilder bufferedLineString(double distance);

  PolygonBuilder polygon();

  <T extends Shape> MultiShapeBuilder<T> multiShape(Class<T> shapeClass);



   */
}
