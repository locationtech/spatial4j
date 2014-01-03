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

package com.spatial4j.core.io;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

@Deprecated
public class JtsShapeReadWriter {

  private static final byte TYPE_POINT = 0;
  private static final byte TYPE_BBOX = 1;
  private static final byte TYPE_GEOM = 2;
  private final JtsSpatialContext ctx;

  public JtsShapeReadWriter(JtsSpatialContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Reads a shape from a byte array, using an internal format written by
   * {@link #writeShapeToBytes(com.spatial4j.core.shape.Shape)}.
   */
  public Shape readShapeFromBytes(final byte[] array, final int offset, final int length) throws InvalidShapeException {
    //NOTE: NO NEED TO VALIDATE/NORMALIZE GEOM AS IT SHOULD HAVE BEEN BEFORE WRITTEN

    ByteBuffer bytes = ByteBuffer.wrap(array, offset, length);
    byte type = bytes.get();
    if (type == TYPE_POINT) {
      return ctx.makePoint(bytes.getDouble(), bytes.getDouble());
    } else if (type == TYPE_BBOX) {
      return ctx.makeRectangle(bytes.getDouble(), bytes.getDouble(),
              bytes.getDouble(), bytes.getDouble());
    } else if (type == TYPE_GEOM) {
      WKBReader reader = new WKBReader(ctx.getGeometryFactory());
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
        //false: don't check for dateline-180 cross or multi-polygon overlaps; this won't happen
        // once it gets written, and we're reading it now
        return ctx.makeShape(geom, false, false);
      } catch (ParseException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      } catch (IOException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      }
    }
    throw new InvalidShapeException("shape not handled: " + type);
  }

  /**
   * Writes shapes in an internal format readable by {@link #readShapeFromBytes(byte[], int, int)}.
   */
  public byte[] writeShapeToBytes(Shape shape) throws IOException {
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

}
