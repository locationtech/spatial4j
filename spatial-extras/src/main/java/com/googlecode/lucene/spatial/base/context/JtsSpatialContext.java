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

package com.googlecode.lucene.spatial.base.context;

import com.googlecode.lucene.spatial.base.shape.JtsEnvelope;
import com.googlecode.lucene.spatial.base.shape.JtsGeometry;
import com.googlecode.lucene.spatial.base.shape.JtsPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Circle;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.CircleImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Locale;

public class JtsSpatialContext extends SpatialContext {

  private static final byte TYPE_POINT = 0;
  private static final byte TYPE_BBOX = 1;
  private static final byte TYPE_GEO = 2;

  public GeometryFactory factory;

  public static JtsSpatialContext GEO_KM = new JtsSpatialContext(DistanceUnits.KILOMETERS);

  @Deprecated
  public JtsSpatialContext() {
    this( null, null, null );
  }
  
  public JtsSpatialContext( DistanceUnits units ) {
    this( null, units, null);
  }

  public JtsSpatialContext(GeometryFactory f, DistanceUnits units, DistanceCalculator calculator) {
    super( units, calculator);
    if (f == null)
      f = new GeometryFactory();
    factory = f;
  }

  @Override
  public Shape readShape(String str) throws InvalidShapeException {
    Shape shape = super.readStandardShape(str);
    if( shape == null ) {
      try {
        WKTReader reader = new WKTReader(factory);
        Geometry geo = reader.read(str);
        if (geo instanceof com.vividsolutions.jts.geom.Point) {
          return new JtsPoint((com.vividsolutions.jts.geom.Point)geo);
        } else if (geo.isRectangle()) {
          return new JtsEnvelope(geo.getEnvelopeInternal());
        }
        return new JtsGeometry(geo);
      } catch(com.vividsolutions.jts.io.ParseException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      }
    }
    return shape;
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
      byte[] bb = writer.write(((JtsGeometry)shape).geo);
      ByteBuffer bytes = ByteBuffer.wrap(new byte[1 + bb.length]);
      bytes.put(TYPE_GEO);
      bytes.put(bb);
      return bytes.array();
    }

    throw new IllegalArgumentException("unsuported shape:" + shape);
  }

  public Shape readShape(final byte[] array, final int offset, final int length) throws InvalidShapeException {
    ByteBuffer bytes = ByteBuffer.wrap(array, offset, length);
    byte type = bytes.get();
    if (type == TYPE_POINT) {
      return new JtsPoint(factory.createPoint(new Coordinate(bytes.getDouble(), bytes.getDouble())));
    } else if (type == TYPE_BBOX) {
      return new JtsEnvelope(
          bytes.getDouble(), bytes.getDouble(),
          bytes.getDouble(), bytes.getDouble());
    } else if (type == TYPE_GEO) {
      WKBReader reader = new WKBReader(factory);
      try {
        return new JtsGeometry(reader.read(new InStream() {
          int off = offset+1; // skip the type marker

          @Override
          public void read(byte[] buf) throws IOException {
            if (off+buf.length > length) {
              throw new InvalidShapeException("Asking for too many bytes");
            }
            for (int i = 0;i < buf.length; i++) {
              buf[i] = array[off + i];
            }
            off += buf.length;
          }
        }));
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
    if (Point.class.isInstance(shape)) {
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      Point point = (Point) shape;
      return nf.format(point.getX()) + " " + nf.format(point.getY());
    } else if (Rectangle.class.isInstance(shape)) {
      Rectangle rect = (Rectangle) shape;
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false );
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      return
        nf.format(rect.getMinX()) + " " +
        nf.format(rect.getMinY()) + " " +
        nf.format(rect.getMaxX()) + " " +
        nf.format(rect.getMaxY());
    } else if (JtsGeometry.class.isInstance(shape)) {
      JtsGeometry geo = (JtsGeometry) shape;
      return geo.geo.toText();
    }
    return shape.toString();
  }

  public Geometry getGeometryFrom(Shape shape) {
    if (JtsGeometry.class.isInstance(shape)) {
      return ((JtsGeometry)shape).geo;
    }
    if (JtsPoint.class.isInstance(shape)) {
      return ((JtsPoint) shape).getJtsPoint();
    }
    if (JtsEnvelope.class.isInstance(shape)) {
      return factory.toGeometry(((JtsEnvelope)shape).envelope);
    }
    if (Circle.class.isInstance(shape)) {
      // TODO, this should maybe pick a bunch of points
      // and make a circle like:
      //  http://docs.codehaus.org/display/GEOTDOC/01+How+to+Create+a+Geometry#01HowtoCreateaGeometry-CreatingaCircle
      // If this crosses the dateline, it could make two parts
      // is there an existing utility that does this?
      Circle circle = (Circle)shape;
      GeometricShapeFactory gsf = new GeometricShapeFactory(factory);
      gsf.setSize(circle.getBoundingBox().getWidth()/2.0f);
      gsf.setNumPoints(100);
      gsf.setBase(new Coordinate(circle.getCenter().getX(),circle.getCenter().getY()));
      return gsf.createCircle();
    }
    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }

  @Override
  public Circle makeCircle(Point point, double distance) {
    return new CircleImpl( point, distance, this );
  }

  @Override
  public Rectangle makeRect(double minX, double maxX, double minY, double maxY) {
    return new JtsEnvelope(new Envelope(minX,maxX,minY,maxY));
  }

  @Override
  public Point makePoint(double x, double y) {
    return new JtsPoint(factory.createPoint(new Coordinate(x, y)));
  }
}
