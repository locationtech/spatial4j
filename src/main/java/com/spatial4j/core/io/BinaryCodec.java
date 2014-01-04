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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class BinaryCodec {
  //type 0; reserved for unkonwn/generic; see readCollection
  protected static final byte
      TYPE_POINT = 1,
      TYPE_RECT = 2,
      TYPE_CIRCLE = 3,
      TYPE_COLL = 4,
      TYPE_GEOM = 5;

  //TODO support BufferedLineString

  protected final SpatialContext ctx;

  //This constructor is mandated by SpatialContextFactory
  public BinaryCodec(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
  }

  public Shape readShape(ByteBuffer byteBuf) {
    byte type = byteBuf.get();
    Shape s = readShapeByTypeIfSupported(byteBuf, type);
    if (s == null)
      throw new IllegalArgumentException("Unsupported shape byte "+type);
    return s;
  }

  public void writeShape(ByteBuffer byteBuf, Shape s) {
    boolean written = writeShapeByTypeIfSupported(byteBuf, s);
    if (!written)
      throw new IllegalArgumentException("Unsupported shape "+s.getClass());
  }

  protected Shape readShapeByTypeIfSupported(ByteBuffer byteBuf, byte type) {
    switch (type) {
      case TYPE_POINT: return readPoint(byteBuf);
      case TYPE_RECT: return readRect(byteBuf);
      case TYPE_CIRCLE: return readCircle(byteBuf);
      case TYPE_COLL: return readCollection(byteBuf);
      default: return null;
    }
  }

  /** Note: writes the type byte even if not supported */
  protected boolean writeShapeByTypeIfSupported(ByteBuffer byteBuf, Shape s) {
    byte type = typeForShape(s);
    byteBuf.put(type);
    return writeShapeByTypeIfSupported(byteBuf, s, type);
    //byteBuf.position(byteBuf.position() - 1);//reset putting type
  }

  protected boolean writeShapeByTypeIfSupported(ByteBuffer byteBuf, Shape s, byte type) {
    switch (type) {
      case TYPE_POINT: writePoint(byteBuf, (Point) s); break;
      case TYPE_RECT: writeRect(byteBuf, (Rectangle) s); break;
      case TYPE_CIRCLE: writeCircle(byteBuf, (Circle) s); break;
      case TYPE_COLL: writeCollection(byteBuf, (ShapeCollection) s); break;
      default:
        return false;
    }
    return true;
  }

  protected byte typeForShape(Shape s) {
    if (s instanceof Point) {
      return TYPE_POINT;
    } else if (s instanceof Rectangle) {
      return TYPE_RECT;
    } else if (s instanceof Circle) {
      return TYPE_CIRCLE;
    } else if (s instanceof ShapeCollection) {
      return TYPE_COLL;
    } else {
      return 0;
    }
  }

  protected double readDim(ByteBuffer byteBuf) {
    return byteBuf.getDouble();
  }

  protected void writeDim(ByteBuffer byteBuf, double v) {
    byteBuf.putDouble(v);
  }

  public Point readPoint(ByteBuffer byteBuf) {
    return ctx.makePoint(readDim(byteBuf), readDim(byteBuf));
  }

  public void writePoint(ByteBuffer byteBuf, Point pt) {
    writeDim(byteBuf, pt.getX());
    writeDim(byteBuf, pt.getY());
  }

  public Rectangle readRect(ByteBuffer byteBuf) {
    return ctx.makeRectangle(readDim(byteBuf), readDim(byteBuf), readDim(byteBuf), readDim(byteBuf));
  }

  public void writeRect(ByteBuffer byteBuf, Rectangle r) {
    writeDim(byteBuf, r.getMinX());
    writeDim(byteBuf, r.getMaxX());
    writeDim(byteBuf, r.getMinY());
    writeDim(byteBuf, r.getMaxY());
  }

  public Circle readCircle(ByteBuffer byteBuf) {
    return ctx.makeCircle(readPoint(byteBuf), readDim(byteBuf));
  }

  public void writeCircle(ByteBuffer byteBuf, Circle c) {
    writePoint(byteBuf, c.getCenter());
    writeDim(byteBuf, c.getRadius());
  }

  public ShapeCollection readCollection(ByteBuffer byteBuf) {
    byte type = byteBuf.get();
    int size = byteBuf.getInt();
    ArrayList<Shape> shapes = new ArrayList<Shape>(size);
    for (int i = 0; i < size; i++) {
      if (type == 0) {
        shapes.add(readShape(byteBuf));
      } else {
        Shape s = readShapeByTypeIfSupported(byteBuf, type);
        if (s == null)
          throw new InvalidShapeException("Unsupported shape byte "+type);
        shapes.add(s);
      }
    }
    return ctx.makeCollection(shapes);
  }

  public void writeCollection(ByteBuffer byteBuf, ShapeCollection col) {
    byte type = (byte) 0;//TODO add type to ShapeCollection
    byteBuf.put(type);
    byteBuf.putInt(col.size());
    for (int i = 0; i < col.size(); i++) {
      Shape s = col.get(i);
      if (type == 0) {
        writeShape(byteBuf, s);
      } else {
        boolean written = writeShapeByTypeIfSupported(byteBuf, s, type);
        if (!written)
          throw new IllegalArgumentException("Unsupported shape type "+s.getClass());
      }
    }
  }

}
