/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.locationtech.spatial4j.shape.jts.JtsPoint;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;
import org.locationtech.jts.geom.*;

import java.util.List;

/**
 * Enhances the default {@link SpatialContext} with support for Polygons (and
 * other geometries) using <a href="https://sourceforge.net/projects/jts-topo-suite/">JTS</a>.
 * To the extent possible, our {@link JtsGeometry} adds some amount of geodetic support over
 * vanilla JTS which only has a Euclidean (flat plane) model.
 */
public class JtsSpatialContext extends SpatialContext {

  public static final JtsSpatialContext GEO;
  static {
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.geo = true;
    GEO = new JtsSpatialContext(factory);
  }

  /**
   * Called by {@link org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory#newSpatialContext()}.
   */
  public JtsSpatialContext(JtsSpatialContextFactory factory) {
    super(factory);
  }

  // TODO I expect to delete this eventually once the other deprecated methods in this class disappear
  @Override
  public JtsShapeFactory getShapeFactory() {
    return (JtsShapeFactory) super.getShapeFactory();
  }

  /**
   * If geom might be a multi geometry of some kind, then might multiple
   * component geometries overlap? Strict OGC says this is invalid but we
   * can accept it by computing the union. Note: Our ShapeCollection mostly
   * doesn't care but it has a method related to this
   * {@link org.locationtech.spatial4j.shape.ShapeCollection#relateContainsShortCircuits()}.
   */
  @Deprecated
  public boolean isAllowMultiOverlap() {
    return getShapeFactory().isAllowMultiOverlap();
  }

  /**
   * Returns the rule used to handle geometry objects that have dateline (aka anti-meridian) crossing considerations.
   */
  @Deprecated
  public DatelineRule getDatelineRule() {
    return getShapeFactory().getDatelineRule();
  }

  /**
   * Returns the rule used to handle errors when creating a JTS {@link Geometry}, particularly after it has been
   * read from one of the {@link ShapeReader}s.
   */
  @Deprecated
  public ValidationRule getValidationRule() {
    return getShapeFactory().getValidationRule();
  }

  /**
   * If JtsGeometry shapes should be automatically "prepared" (i.e. optimized) when read via from a {@link ShapeReader}.
   *
   * @see org.locationtech.spatial4j.shape.jts.JtsGeometry#index()
   */
  @Deprecated
  public boolean isAutoIndex() {
    return getShapeFactory().isAutoIndex();
  }

  /**
   * Gets a JTS {@link Geometry} for the given {@link Shape}. Some shapes hold a
   * JTS geometry whereas new ones must be created for the rest.
   * @param shape Not null
   * @return Not null
   */
  @Deprecated
  public Geometry getGeometryFrom(Shape shape) {
    return getShapeFactory().getGeometryFrom(shape);
  }

  /** Should {@link #makePoint(double, double)} return {@link JtsPoint}? */
  @Deprecated
  public boolean useJtsPoint() {
    return getShapeFactory().useJtsPoint();
  }

  /** Should {@link #makeLineString(java.util.List)} return {@link JtsGeometry}? */
  @Deprecated
  public boolean useJtsLineString() {
    return getShapeFactory().useJtsLineString();
  }

  /**
   * INTERNAL Usually creates a JtsGeometry, potentially validating, repairing, and indexing ("preparing"). This method
   * is intended for use by {@link ShapeReader} instances.
   *
   * If given a direct instance of {@link GeometryCollection} then it's contents will be
   * recursively converted and then the resulting list will be passed to
   * {@link SpatialContext#makeCollection(List)} and returned.
   *
   * If given a {@link org.locationtech.jts.geom.Point} then {@link SpatialContext#makePoint(double, double)}
   * is called, which will return a {@link JtsPoint} if {@link JtsSpatialContext#useJtsPoint()}; otherwise
   * a standard Spatial4j Point is returned.
   *
   * If given a {@link LineString} and if {@link JtsSpatialContext#useJtsLineString()} is true then
   * then the geometry's parts are exposed to call {@link SpatialContext#makeLineString(List)}.
   */
  @Deprecated
  public Shape makeShapeFromGeometry(Geometry geom) {
    return getShapeFactory().makeShapeFromGeometry(geom);
  }

  /**
   * INTERNAL
   * @see #makeShape(org.locationtech.jts.geom.Geometry)
   *
   * @param geom Non-null
   * @param dateline180Check if both this is true and {@link #isGeo()}, then JtsGeometry will check
   *                         for adjacent coordinates greater than 180 degrees longitude apart, and
   *                         it will do tricks to make that line segment (and the shape as a whole)
   *                         cross the dateline even though JTS doesn't have geodetic support.
   * @param allowMultiOverlap See {@link #isAllowMultiOverlap()}.
   */
  @Deprecated
  public JtsGeometry makeShape(Geometry geom, boolean dateline180Check, boolean allowMultiOverlap) {
    return getShapeFactory().makeShape(geom, dateline180Check, allowMultiOverlap);
  }

  /**
   * INTERNAL: Creates a {@link Shape} from a JTS {@link Geometry}. Generally, this shouldn't be
   * called when one of the other factory methods are available, such as for points. The caller
   * needs to have done some verification/normalization of the coordinates by now, if any.  Also,
   * note that direct instances of {@link GeometryCollection} isn't supported.
   *
   * Instead of calling this method, consider {@link JtsShapeFactory#makeShapeFromGeometry(Geometry)}
   * which
   */
  @Deprecated
  public JtsGeometry makeShape(Geometry geom) {
    return getShapeFactory().makeShape(geom);
  }

  @Deprecated
  public GeometryFactory getGeometryFactory() {
    return getShapeFactory().getGeometryFactory();
  }

  @Override
  public String toString() {
    if (this.equals(GEO)) {
      return GEO.getClass().getSimpleName()+".GEO";
    } else {
      return super.toString();
    }
  }

  /**
   * INTERNAL: Returns a Rectangle of the JTS {@link Envelope} (bounding box) of the given {@code geom}.  This asserts
   * that {@link Geometry#isRectangle()} is true.  This method reacts to the {@link DatelineRule} setting.
   * @param geom non-null
   * @return null equivalent Rectangle.
   */
  @Deprecated
  public Rectangle makeRectFromRectangularPoly(Geometry geom) {
    return getShapeFactory().makeRectFromRectangularPoly(geom);
  }
}
