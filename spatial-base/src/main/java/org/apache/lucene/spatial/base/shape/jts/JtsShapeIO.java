/**
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

package org.apache.lucene.spatial.base.shape.jts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.AbstractShapeIO;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

public class JtsShapeIO extends AbstractShapeIO {

  private static final byte TYPE_POINT = 0;
  private static final byte TYPE_BBOX = 1;
  private static final byte TYPE_GEO = 2;

  public GeometryFactory factory;

  public JtsShapeIO() {
    this( new GeometryFactory(), DistanceUnits.KILOMETERS );
  }

  public JtsShapeIO( DistanceUnits units ) {
    this( new GeometryFactory(), units );
  }

  public JtsShapeIO(GeometryFactory f, DistanceUnits units) {
    super( units );
    factory = f;
  }

  public Shape readShape(String str) throws InvalidShapeException {
    Shape shape = super.readStandardShape(str);
    if( shape == null ) {
      try {
        WKTReader reader = new WKTReader(factory);
        Geometry geo = reader.read(str);
        if (geo instanceof com.vividsolutions.jts.geom.Point) {
          return new JtsPoint2D((com.vividsolutions.jts.geom.Point)geo);
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

    if (BBox.class.isInstance(shape)) {
      BBox b = (BBox) shape;
      ByteBuffer bytes = ByteBuffer.wrap(new byte[1 + (4 * 8)]);
      bytes.put(TYPE_BBOX);
      bytes.putDouble(b.getMinX());
      bytes.putDouble(b.getMaxX());
      bytes.putDouble(b.getMinY());
      bytes.putDouble(b.getMaxY());
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
      return new JtsPoint2D(factory.createPoint(new Coordinate(bytes.getDouble(), bytes.getDouble())));
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
    } else if (BBox.class.isInstance(shape)) {
      BBox bbox = (BBox) shape;
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false );
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      return
        nf.format(bbox.getMinX()) + " " +
        nf.format(bbox.getMinY()) + " " +
        nf.format(bbox.getMaxX()) + " " +
        nf.format(bbox.getMaxY());
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
    if (JtsPoint2D.class.isInstance(shape)) {
      return ((JtsPoint2D)shape).getPoint();
    }
    if (JtsEnvelope.class.isInstance(shape)) {
      return factory.toGeometry(((JtsEnvelope)shape).envelope);
    }
    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }

  @Override
  public BBox makeBBox(double minX, double maxX, double minY, double maxY) {
    return new JtsEnvelope(new Envelope(minX,maxX,minY,maxY));
  }

  @Override
  public Point makePoint(double x, double y) {
    return new JtsPoint2D(factory.createPoint(new Coordinate(x, y)));
  }
}
