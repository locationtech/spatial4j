/*******************************************************************************
 * Copyright (c) 2016 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import org.locationtech.spatial4j.context.SpatialContext;

import java.util.List;

/**
 * A factory for {@link Shape}s.
 * Stateless and thread-safe, except for any returned builders.
 */
public interface ShapeFactory {

  SpatialContext getSpatialContext();

  /** If true then {@link #normX(double)} will wrap longitudes outside of the standard
   * geodetic boundary into it. Example: 181 will become -179. */
  boolean isNormWrapLongitude();

  // TODO annoying that a ShapeReader must remember to call norm* methods.  Perhaps
  //  there should be another shapeFactory impl for shape reading?  :-/  Or not.

  /** Normalize the 'x' dimension. Might reduce precision or wrap it to be within the bounds. This
   * is called by {@link org.locationtech.spatial4j.io.ShapeReader}s before creating a shape. */
  double normX(double x);

  /** @see #normX(double)  */
  double normY(double y);

  /** (disclaimer: the Z dimension isn't fully supported)
   * @see #normX(double) */
  double normZ(double z);

  /**
   * Called to normalize a value that isn't X or Y or Z. X &amp; Y &amp; Z are normalized via
   * {@link org.locationtech.spatial4j.context.SpatialContext#normX(double)} &amp; normY &amp; normZ. This
   * is called by a {@link org.locationtech.spatial4j.io.ShapeReader} before creating a shape.
   */
  double normDist(double d);

  /** Ensure fits in the world bounds. It's called by any shape factory method that
   * gets an 'x' dimension. */
  void verifyX(double x);

  /** @see #verifyX(double)  */
  void verifyY(double y);

  /** (disclaimer: the Z dimension isn't fully supported)
   *  @see #verifyX(double)  */
  void verifyZ(double z);

  /** Construct a point. */
  Point pointXY(double x, double y);

  /** Construct a point of latitude, longitude coordinates */
  Point pointLatLon(double latitude, double longitude);

  /** Construct a point of 3 dimensions.  The implementation might ignore unsupported
   * dimensions like 'z' or throw an error. */
  Point pointXYZ(double x, double y, double z);

  /** Construct a rectangle. */
  Rectangle rect(Point lowerLeft, Point upperRight);

  /**
   * Construct a rectangle. If just one longitude is on the dateline (+/- 180) and if
   * {@link SpatialContext#isGeo()}
   * then potentially adjust its sign to ensure the rectangle does not cross the
   * dateline (aka anti-meridian).
   */
  Rectangle rect(double minX, double maxX, double minY, double maxY);

  /** Construct a circle. The units of "distance" should be the same as x &amp; y. */
  Circle circle(double x, double y, double distance);

  /** Construct a circle. The units of "distance" should be the same as x &amp; y. */
  Circle circle(Point point, double distance);

  /** Constructs a line string with a possible buffer. It's an ordered sequence of connected vertexes,
   * with a buffer distance along the line in all directions. There
   * is no official shape/interface for it so we just return Shape. */
  @Deprecated // use a builder
  Shape lineString(List<Point> points, double buf);

  /** Construct a ShapeCollection, analogous to an OGC GeometryCollection. */
  @Deprecated // use a builder
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

  /** (Builder) Constructs a MultiPoint. */
  MultiPointBuilder multiPoint();

  /** (Builder) Constructs a MultiLineString, or possibly the result of that buffered. */
  MultiLineStringBuilder multiLineString();

  /** (Builder) Constructs a MultiPolygon. */
  MultiPolygonBuilder multiPolygon();

  // misc:
  //Shape buffer(Shape shape);  ?

  // TODO need Polygon shape
  // TODO need LineString shape
  // TODO need BufferedLineString shape
  // TODO need ShapeCollection to be typed

  /** Builds a point and returns the generic specified type (usually whatever "this" is). */
  interface PointsBuilder<T> {
    /** @see ShapeFactory#pointXY(double, double) */
    T pointXY(double x, double y);
    /** @see ShapeFactory#pointXYZ(double, double, double) */
    T pointXYZ(double x, double y, double z);
    /** @see ShapeFactory#pointLatLon(double, double)  */
    T pointLatLon(double latitude, double longitude);
  }

  /** @see #lineString() */
  interface LineStringBuilder extends PointsBuilder<LineStringBuilder> {
    // TODO add dimensionality hint method?

    LineStringBuilder buffer(double distance);

    Shape build();
  }

  /** @see #polygon() */
  interface PolygonBuilder extends PointsBuilder<PolygonBuilder> {
    // TODO add dimensionality hint method?

    /** Starts a new hole. You must add at least 4 points; furthermore the first and last must be the same.
     * And don't forget to call {@link HoleBuilder#endHole()}! */
    HoleBuilder hole();

    /** Builds the polygon and renders this builder instance invalid. */
    Shape build();// never a Rect

    Shape buildOrRect();

    interface HoleBuilder extends PointsBuilder<HoleBuilder> {
      /** Finishes the hole and returns the {@link PolygonBuilder}.*/
      PolygonBuilder endHole();
    }
  }

  // TODO add dimensionality hint method to the multi* builders?

  /** @see #multiShape(Class) */
  interface MultiShapeBuilder<T extends Shape> {
    // TODO add dimensionality hint method?

    MultiShapeBuilder<T> add(T shape);

    //ShapeCollection<T> build(); TODO wait till it's a typed interface
    Shape build();
  }

  /** @see #multiPoint() */
  interface MultiPointBuilder extends PointsBuilder<MultiPointBuilder> {

    Shape build();  // TODO MultiShape<Point>
  }

  /** @see #multiLineString() */
  interface MultiLineStringBuilder {

    LineStringBuilder lineString();

    MultiLineStringBuilder add(LineStringBuilder lineStringBuilder);

    Shape build(); // TODO MultiShape<LineString>
  }

  /** @see #multiPolygon() */
  interface MultiPolygonBuilder {

    PolygonBuilder polygon();

    MultiPolygonBuilder add(PolygonBuilder polygonBuilder);

    Shape build(); // TODO MultiShape<Polygon>
  }
}
