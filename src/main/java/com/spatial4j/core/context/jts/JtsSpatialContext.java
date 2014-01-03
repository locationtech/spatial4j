/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.context.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import java.util.ArrayList;
import java.util.Collection;
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

  protected final GeometryFactory geometryFactory;

  protected final boolean allowMultiOverlap;
  protected final boolean useJtsPoint;
  protected final boolean useJtsLineString;

  /**
   * Called by {@link com.spatial4j.core.context.jts.JtsSpatialContextFactory#newSpatialContext()}.
   */
  public JtsSpatialContext(JtsSpatialContextFactory factory) {
    super(factory);
    this.geometryFactory = factory.getGeometryFactory();

    this.allowMultiOverlap = factory.allowMultiOverlap;
    this.useJtsPoint = factory.useJtsPoint;
    this.useJtsLineString = factory.useJtsLineString;
  }

  /**
   * If geom might be a multi geometry of some kind, then might multiple
   * component geometries overlap? Strict OGC says this is invalid but we
   * can accept it by computing the union. Note: Our ShapeCollection mostly
   * doesn't care but it has a method related to this
   * {@link com.spatial4j.core.shape.ShapeCollection#relateContainsShortCircuits()}.
   */
  public boolean isAllowMultiOverlap() {
    return allowMultiOverlap;
  }

  @Override
  public double normX(double x) {
    x = super.normX(x);
    return geometryFactory.getPrecisionModel().makePrecise(x);
  }

  @Override
  public double normY(double y) {
    y = super.normY(y);
    return geometryFactory.getPrecisionModel().makePrecise(y);
  }

  @Override
  public String toString(Shape shape) {
    //Note: this logic is from the defunct JtsShapeReadWriter
    if (shape instanceof JtsGeometry) {
      JtsGeometry jtsGeom = (JtsGeometry) shape;
      return jtsGeom.getGeom().toText();
    }
    //Note: doesn't handle ShapeCollection or BufferedLineString
    return super.toString(shape);
  }

  /**
   * Gets a JTS {@link Geometry} for the given {@link Shape}. Some shapes hold a
   * JTS geometry whereas new ones must be created for the rest.
   * @param shape Not null
   * @return Not null
   */
  public Geometry getGeometryFrom(Shape shape) {
    if (shape instanceof JtsGeometry) {
      return ((JtsGeometry)shape).getGeom();
    }
    if (shape instanceof JtsPoint) {
      return ((JtsPoint) shape).getGeom();
    }
    if (shape instanceof Point) {
      Point point = (Point) shape;
      return geometryFactory.createPoint(new Coordinate(point.getX(),point.getY()));
    }
    if (shape instanceof Rectangle) {
      Rectangle r = (Rectangle)shape;
      if (r.getCrossesDateLine()) {
        Collection<Geometry> pair = new ArrayList<Geometry>(2);
        pair.add(geometryFactory.toGeometry(new Envelope(
                r.getMinX(), getWorldBounds().getMaxX(), r.getMinY(), r.getMaxY())));
        pair.add(geometryFactory.toGeometry(new Envelope(
                getWorldBounds().getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY())));
        return geometryFactory.buildGeometry(pair);//a MultiPolygon or MultiLineString
      } else {
        return geometryFactory.toGeometry(new Envelope(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY()));
      }
    }
    if (shape instanceof Circle) {
      // FYI Some interesting code for this is here:
      //  http://docs.codehaus.org/display/GEOTDOC/01+How+to+Create+a+Geometry#01HowtoCreateaGeometry-CreatingaCircle
      //TODO This should ideally have a geodetic version
      Circle circle = (Circle)shape;
      if (circle.getBoundingBox().getCrossesDateLine())
        throw new IllegalArgumentException("Doesn't support dateline cross yet: "+circle);//TODO
      GeometricShapeFactory gsf = new GeometricShapeFactory(geometryFactory);
      gsf.setSize(circle.getBoundingBox().getWidth());
      gsf.setNumPoints(4*25);//multiple of 4 is best
      gsf.setCentre(new Coordinate(circle.getCenter().getX(), circle.getCenter().getY()));
      return gsf.createCircle();
    }
    //TODO add BufferedLineString
    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }

  /** Should {@link #makePoint(double, double)} return {@link JtsPoint}? */
  public boolean useJtsPoint() {
    return useJtsPoint;
  }

  @Override
  public Point makePoint(double x, double y) {
    if (!useJtsPoint())
      return super.makePoint(x, y);
    //A Jts Point is fairly heavyweight!  TODO could/should we optimize this? SingleCoordinateSequence
    verifyX(x);
    verifyY(y);
    Coordinate coord = Double.isNaN(x) ? null : new Coordinate(x, y);
    return new JtsPoint(geometryFactory.createPoint(coord), this);
  }

  /** Should {@link #makeLineString(java.util.List)} return {@link JtsGeometry}? */
  public boolean useJtsLineString() {
    //BufferedLineString doesn't yet do dateline cross, and can't yet be relate()'ed with a
    // JTS geometry
    return useJtsLineString;
  }

  @Override
  public Shape makeLineString(List<Point> points) {
    if (!useJtsLineString())
      return super.makeLineString(points);
    //convert List<Point> to Coordinate[]
    Coordinate[] coords = new Coordinate[points.size()];
    for (int i = 0; i < coords.length; i++) {
      Point p = points.get(i);
      if (p instanceof JtsPoint) {
        JtsPoint jtsPoint = (JtsPoint) p;
        coords[i] = jtsPoint.getGeom().getCoordinate();
      } else {
        coords[i] = new Coordinate(p.getX(), p.getY());
      }
    }
    LineString lineString = geometryFactory.createLineString(coords);
    return makeShape(lineString);
  }

  /**
   * INTERNAL
   * @see #makeShape(com.vividsolutions.jts.geom.Geometry)
   *
   * @param geom Non-null
   * @param dateline180Check if both this is true and {@link #isGeo()}, then JtsGeometry will check
   *                         for adjacent coordinates greater than 180 degrees longitude apart, and
   *                         it will do tricks to make that line segment (and the shape as a whole)
   *                         cross the dateline even though JTS doesn't have geodetic support.
   * @param allowMultiOverlap See {@link #isAllowMultiOverlap()}.
   */
  public JtsGeometry makeShape(Geometry geom, boolean dateline180Check, boolean allowMultiOverlap) {
    return new JtsGeometry(geom, this, dateline180Check, allowMultiOverlap);
  }

  /**
   * INTERNAL: Creates a {@link Shape} from a JTS {@link Geometry}. Generally, this shouldn't be
   * called when one of the other factory methods are available, such as for points. The caller
   * needs to have done some verification/normalization of the coordinates by now, if any.
   */
  public JtsGeometry makeShape(Geometry geom) {
    return makeShape(geom, true/*dateline180Check*/, allowMultiOverlap);
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public String toString() {
    if (this.equals(GEO)) {
      return GEO.getClass().getSimpleName()+".GEO";
    } else {
      return super.toString();
    }
  }

}
