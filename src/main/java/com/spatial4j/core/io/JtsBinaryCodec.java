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
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Writes shapes in WKB, if it isn't
 */
public class JtsBinaryCodec extends BinaryCodec {

  protected final boolean useFloat;//instead of double

  public JtsBinaryCodec(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    //note: ctx.geometryFactory hasn't been set yet
    useFloat = (factory.precisionModel.getType() == PrecisionModel.FLOATING_SINGLE);
  }

  @Override
  protected double readDim(ByteBuffer byteBuf) {
    if (useFloat)
      return byteBuf.getFloat();
    return super.readDim(byteBuf);
  }

  @Override
  protected void writeDim(ByteBuffer byteBuf, double v) {
    if (useFloat)
      byteBuf.putFloat((float)v);
    else
      super.writeDim(byteBuf, v);
  }

  @Override
  protected byte typeForShape(Shape s) {
    byte type = super.typeForShape(s);
    if (type == 0) {
      type = TYPE_GEOM;//handles everything
    }
    return type;
  }

  @Override
  protected Shape readShapeByTypeIfSupported(final ByteBuffer byteBuf, byte type) {
    if (type != TYPE_GEOM)
      return super.readShapeByTypeIfSupported(byteBuf, type);
    return readJtsGeom(byteBuf);
  }

  @Override
  protected boolean writeShapeByTypeIfSupported(ByteBuffer byteBuf, Shape s, byte type) {
    if (type != TYPE_GEOM)
      return super.writeShapeByTypeIfSupported(byteBuf, s, type);
    writeJtsGeom(byteBuf, s);
    return true;
  }

  public Shape readJtsGeom(final ByteBuffer byteBuf) {
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    WKBReader reader = new WKBReader(ctx.getGeometryFactory());
    try {
      InStream inStream = new InStream() {//a strange JTS abstraction
        boolean first = true;
        @Override
        public void read(byte[] buf) throws IOException {
          if (first) {//we don't write JTS's leading BOM so synthesize reading it
            if (buf.length != 1)
              throw new IllegalStateException("Expected initial read of one byte, not: " + buf.length);
            buf[0] = WKBConstants.wkbXDR;//0
            first = false;
          } else {
            //TODO for performance, specialize for common array lengths: 1, 4, 8
            byteBuf.get(buf);
          }
        }
      };
      Geometry geom = reader.read(inStream);
      //false: don't check for dateline-180 cross or multi-polygon overlaps; this won't happen
      // once it gets written, and we're reading it now
      return ctx.makeShape(geom, false, false);
    } catch (ParseException ex) {
      throw new InvalidShapeException("error reading WKT", ex);
    } catch (IOException ex) {
      throw new InvalidShapeException("error reading WKT", ex);
    }
  }

  public void writeJtsGeom(ByteBuffer byteBuf, Shape s) {
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    Geometry geom = ctx.getGeometryFrom(s);//might even translate it
    WKBWriter writer = new WKBWriter();
    byte[] bb = writer.write(geom);
    if (bb[0] != WKBConstants.wkbXDR)//the default
      throw new IllegalStateException("Unexpected WKB byte order mark");
    byteBuf.put(bb, 1, bb.length - 1);//skip byte order mark
  }
}
