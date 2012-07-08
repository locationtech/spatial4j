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

  private static final byte TYPE_POINT = 0;
  private static final byte TYPE_BBOX = 1;
  private static final byte TYPE_GEOM = 2;

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

  /**
   * Reads the standard shape format + WKT.
   */
  @Override
  public Shape readShape(String str) throws InvalidShapeException {
    Shape shape = super.readStandardShape(str);
    if( shape == null ) {
      try {
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geom = reader.read(str);

        //Normalize coordinates to geo boundary
        normalizeCoordinates(geom);

        if (geom instanceof com.vividsolutions.jts.geom.Point) {
          return new JtsPoint((com.vividsolutions.jts.geom.Point)geom);
        } else if (geom.isRectangle()) {
          boolean crossesDateline = false;
          if (isGeo()) {
            //Polygon points are supposed to be counter-clockwise order. If JTS says it is clockwise, then
            // it's actually a dateline crossing rectangle.
            crossesDateline = ! CGAlgorithms.isCCW(geom.getCoordinates());
          }
          Envelope env = geom.getEnvelopeInternal();
          if (crossesDateline)
            return new RectangleImpl(env.getMaxX(),env.getMinX(),env.getMinY(),env.getMaxY());
          else
            return new RectangleImpl(env.getMinX(),env.getMaxX(),env.getMinY(),env.getMaxY());
        }
        return new JtsGeometry(geom,this);
      } catch(com.vividsolutions.jts.io.ParseException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      }
    }
    return shape;
  }

  private void normalizeCoordinates(Geometry geom) {
    //TODO add configurable skip flag if input is in the right coordinates
    if (!isGeo())
      return;
    geom.apply(new CoordinateSequenceFilter() {
      boolean changed = false;
      @Override
      public void filter(CoordinateSequence seq, int i) {
        double x = seq.getX(i);
        double xNorm = normX(x);
        if (x != xNorm) {
          changed = true;
          seq.setOrdinate(i,CoordinateSequence.X,xNorm);
        }
        double y = seq.getY(i);
        double yNorm = normY(y);
        if (y != yNorm) {
          changed = true;
          seq.setOrdinate(i,CoordinateSequence.Y,yNorm);
        }
      }

      @Override
      public boolean isDone() { return false; }

      @Override
      public boolean isGeometryChanged() { return changed; }
    });
  }

  public byte[] toBytes(Shape shape) throws IOException {
    if (Point.class.isInstance(shape)) {
      ByteBuffer bytes = ByteBuffer.wrap(new byte[1 + (2 * 8)]);
      Point p = (Point) shape;
      bytes.put(TYPE_POINT);
      bytes.putDouble(p.getX());
      bytes.putDouble(p.getY());
      return bytes.array();
    }

    if (Rectangle.class.isInstance(shape)) {
      Rectangle rect = (Rectangle) shape;
      ByteBuffer bytes = ByteBuffer.wrap(new byte[1 + (4 * 8)]);
      bytes.put(TYPE_BBOX);
      bytes.putDouble(rect.getMinX());
      bytes.putDouble(rect.getMaxX());
      bytes.putDouble(rect.getMinY());
      bytes.putDouble(rect.getMaxY());
      return bytes.array();
    }

    if (JtsGeometry.class.isInstance(shape)) {
      WKBWriter writer = new WKBWriter();
      byte[] bb = writer.write(((JtsGeometry)shape).getGeom());
      ByteBuffer bytes = ByteBuffer.wrap(new byte[1 + bb.length]);
      bytes.put(TYPE_GEOM);
      bytes.put(bb);
      return bytes.array();
    }

    throw new IllegalArgumentException("unsuported shape:" + shape);
  }

  public Shape readShape(final byte[] array, final int offset, final int length) throws InvalidShapeException {
    ByteBuffer bytes = ByteBuffer.wrap(array, offset, length);
    byte type = bytes.get();
    if (type == TYPE_POINT) {
      return new JtsPoint(geometryFactory.createPoint(new Coordinate(bytes.getDouble(), bytes.getDouble())));
    } else if (type == TYPE_BBOX) {
      return new RectangleImpl(
          bytes.getDouble(), bytes.getDouble(),
          bytes.getDouble(), bytes.getDouble());
    } else if (type == TYPE_GEOM) {
      WKBReader reader = new WKBReader(geometryFactory);
      try {
        Geometry geom = reader.read(new InStream() {
          int off = offset + 1; // skip the type marker

          @Override
          public void read(byte[] buf) throws IOException {
            if (off + buf.length > length) {
              throw new InvalidShapeException("Asking for too many bytes");
            }
            System.arraycopy(array, off, buf, 0, buf.length);
            off += buf.length;
          }
        });
        normalizeCoordinates(geom);
        return new JtsGeometry(geom, this);
      } catch(ParseException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      } catch (IOException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      }
    }
    throw new InvalidShapeException("shape not handled: " + type);
  }

  @Override
  public String toString(Shape shape) {
    if (shape instanceof JtsGeometry) {
      JtsGeometry jtsGeom = (JtsGeometry) shape;
      return jtsGeom.getGeom().toText();
    }
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
