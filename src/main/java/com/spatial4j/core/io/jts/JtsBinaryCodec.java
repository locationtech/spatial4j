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

package com.spatial4j.core.io.jts;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.io.BinaryCodec;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.OutStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBConstants;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writes shapes in WKB, if it isn't otherwise supported by the superclass.
 */
public class JtsBinaryCodec extends BinaryCodec {

  protected final boolean useFloat;//instead of double

  public JtsBinaryCodec(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    //note: ctx.geometryFactory hasn't been set yet
    useFloat = (factory.precisionModel.getType() == PrecisionModel.FLOATING_SINGLE);
  }

  @Override
  protected double readDim(DataInput dataInput) throws IOException {
    if (useFloat)
      return dataInput.readFloat();
    return super.readDim(dataInput);
  }

  @Override
  protected void writeDim(DataOutput dataOutput, double v) throws IOException {
    if (useFloat)
      dataOutput.writeFloat((float) v);
    else
      super.writeDim(dataOutput, v);
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
  protected Shape readShapeByTypeIfSupported(final DataInput dataInput, byte type) throws IOException {
    if (type != TYPE_GEOM)
      return super.readShapeByTypeIfSupported(dataInput, type);
    return readJtsGeom(dataInput);
  }

  @Override
  protected boolean writeShapeByTypeIfSupported(DataOutput dataOutput, Shape s, byte type) throws IOException {
    if (type != TYPE_GEOM)
      return super.writeShapeByTypeIfSupported(dataOutput, s, type);
    writeJtsGeom(dataOutput, s);
    return true;
  }

  public Shape readJtsGeom(final DataInput dataInput) throws IOException {
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
            dataInput.readFully(buf);
          }
        }
      };
      Geometry geom = reader.read(inStream);
      //false: don't check for dateline-180 cross or multi-polygon overlaps; this won't happen
      // once it gets written, and we're reading it now
      return ctx.makeShape(geom, false, false);
    } catch (ParseException ex) {
      throw new InvalidShapeException("error reading WKT", ex);
    }
  }

  public void writeJtsGeom(final DataOutput dataOutput, Shape s) throws IOException {
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    Geometry geom = ctx.getGeometryFrom(s);//might even translate it
    new WKBWriter().write(geom, new OutStream() {//a strange JTS abstraction
      boolean first = true;
      @Override
      public void write(byte[] buf, int len) throws IOException {
        if (first) {
          first = false;
          //skip byte order mark
          if (len != 1 || buf[0] != WKBConstants.wkbXDR)//the default
            throw new IllegalStateException("Unexpected WKB byte order mark");
          return;
        }
        dataOutput.write(buf, 0, len);
      }
    });
  }
}
