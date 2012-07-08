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

package com.spatial4j.core.context.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUnits;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.io.JtsShapeReadWriter;
import com.spatial4j.core.io.ShapeReadWriter;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Enhances the default {@link SpatialContext} with support for Polygons (and
 * other geometry) plus
 * reading <a href="http://en.wikipedia.org/wiki/Well-known_text">WKT</a>. The
 * popular <a href="https://sourceforge.net/projects/jts-topo-suite/">JTS</a>
 * library does the heavy lifting.
 */
public class JtsSpatialContext extends SpatialContext {

  public static JtsSpatialContext GEO_KM = new JtsSpatialContext(DistanceUnits.KILOMETERS);

  private final GeometryFactory geometryFactory;

  public JtsSpatialContext( DistanceUnits units ) {
    this(null, units, null, null);
  }

  /**
   * See {@link SpatialContext#SpatialContext(com.spatial4j.core.distance.DistanceUnits, com.spatial4j.core.distance.DistanceCalculator, com.spatial4j.core.shape.Rectangle)}.
   *
   * @param geometryFactory optional
   */
  public JtsSpatialContext(GeometryFactory geometryFactory, DistanceUnits units, DistanceCalculator calculator, Rectangle worldBounds) {
    super(units, calculator, worldBounds);
    this.geometryFactory = geometryFactory == null ? new GeometryFactory() : geometryFactory;
  }

  protected ShapeReadWriter makeShapeReadWriter() {
    return new JtsShapeReadWriter(this);
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
      if (r.getCrossesDateLine())
        throw new IllegalArgumentException("Doesn't support dateline cross yet: "+r);//TODO
      return geometryFactory.toGeometry(new Envelope(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY()));
    }
    if (shape instanceof Circle) {
      // TODO, this should maybe pick a bunch of points
      // and make a circle like:
      //  http://docs.codehaus.org/display/GEOTDOC/01+How+to+Create+a+Geometry#01HowtoCreateaGeometry-CreatingaCircle
      // If this crosses the dateline, it could make two parts
      // is there an existing utility that does this?
      Circle circle = (Circle)shape;
      if (circle.getBoundingBox().getCrossesDateLine())
        throw new IllegalArgumentException("Doesn't support dateline cross yet: "+circle);//TODO
      GeometricShapeFactory gsf = new GeometricShapeFactory(geometryFactory);
      gsf.setSize(circle.getBoundingBox().getWidth()/2.0f);
      gsf.setNumPoints(4*25);//multiple of 4 is best
      gsf.setBase(new Coordinate(circle.getCenter().getX(),circle.getCenter().getY()));
      return gsf.createCircle();
    }
    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }
  
  @Override
  public Point makePoint(double x, double y) {
    //A Jts Point is fairly heavyweight!  TODO could/should we optimize this?
    x = normX(x);
    y = normY(y);
    return new JtsPoint(geometryFactory.createPoint(new Coordinate(x, y)));
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public String toString() {
    if (this.equals(GEO_KM)) {
      return GEO_KM.getClass().getSimpleName()+".GEO_KM";
    } else {
      return super.toString();
    }
  }
}
