/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A binary shape format. It is <em>not</em> designed to be a published standard, unlike Well Known
 * Binary (WKB). The initial release is simple but it could get more optimized to use fewer bytes or
 * to write &amp; read pre-computed index structures.
 * <p>
 * Immutable and thread-safe.
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

  public Shape readShape(DataInput dataInput) throws IOException {
    byte type = dataInput.readByte();
    Shape s = readShapeByTypeIfSupported(dataInput, type);
    if (s == null)
      throw new IllegalArgumentException("Unsupported shape byte "+type);
    return s;
  }

  public void writeShape(DataOutput dataOutput, Shape s) throws IOException {
    boolean written = writeShapeByTypeIfSupported(dataOutput, s);
    if (!written)
      throw new IllegalArgumentException("Unsupported shape "+s.getClass());
  }

  protected Shape readShapeByTypeIfSupported(DataInput dataInput, byte type) throws IOException {
    switch (type) {
      case TYPE_POINT: return readPoint(dataInput);
      case TYPE_RECT: return readRect(dataInput);
      case TYPE_CIRCLE: return readCircle(dataInput);
      case TYPE_COLL: return readCollection(dataInput);
      default: return null;
    }
  }

  /** Note: writes the type byte even if not supported */
  protected boolean writeShapeByTypeIfSupported(DataOutput dataOutput, Shape s) throws IOException {
    byte type = typeForShape(s);
    dataOutput.writeByte(type);
    return writeShapeByTypeIfSupported(dataOutput, s, type);
    //dataOutput.position(dataOutput.position() - 1);//reset putting type
  }

  protected boolean writeShapeByTypeIfSupported(DataOutput dataOutput, Shape s, byte type) throws IOException {
    switch (type) {
      case TYPE_POINT: writePoint(dataOutput, (Point) s); break;
      case TYPE_RECT: writeRect(dataOutput, (Rectangle) s); break;
      case TYPE_CIRCLE: writeCircle(dataOutput, (Circle) s); break;
      case TYPE_COLL: writeCollection(dataOutput, (ShapeCollection) s); break;
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

  protected double readDim(DataInput dataInput) throws IOException {
    return dataInput.readDouble();
  }

  protected void writeDim(DataOutput dataOutput, double v) throws IOException {
    dataOutput.writeDouble(v);
  }

  public Point readPoint(DataInput dataInput) throws IOException {
    return ctx.makePoint(readDim(dataInput), readDim(dataInput));
  }

  public void writePoint(DataOutput dataOutput, Point pt) throws IOException {
    writeDim(dataOutput, pt.getX());
    writeDim(dataOutput, pt.getY());
  }

  public Rectangle readRect(DataInput dataInput) throws IOException {
    return ctx.makeRectangle(readDim(dataInput), readDim(dataInput), readDim(dataInput), readDim(dataInput));
  }

  public void writeRect(DataOutput dataOutput, Rectangle r) throws IOException {
    writeDim(dataOutput, r.getMinX());
    writeDim(dataOutput, r.getMaxX());
    writeDim(dataOutput, r.getMinY());
    writeDim(dataOutput, r.getMaxY());
  }

  public Circle readCircle(DataInput dataInput) throws IOException {
    return ctx.makeCircle(readPoint(dataInput), readDim(dataInput));
  }

  public void writeCircle(DataOutput dataOutput, Circle c) throws IOException {
    writePoint(dataOutput, c.getCenter());
    writeDim(dataOutput, c.getRadius());
  }

  public ShapeCollection readCollection(DataInput dataInput) throws IOException {
    byte type = dataInput.readByte();
    int size = dataInput.readInt();
    ArrayList<Shape> shapes = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      if (type == 0) {
        shapes.add(readShape(dataInput));
      } else {
        Shape s = readShapeByTypeIfSupported(dataInput, type);
        if (s == null)
          throw new InvalidShapeException("Unsupported shape byte "+type);
        shapes.add(s);
      }
    }
    return ctx.makeCollection(shapes);
  }

  public void writeCollection(DataOutput dataOutput, ShapeCollection col) throws IOException {
    byte type = (byte) 0;//TODO add type to ShapeCollection
    dataOutput.writeByte(type);
    dataOutput.writeInt(col.size());
    for (int i = 0; i < col.size(); i++) {
      Shape s = col.get(i);
      if (type == 0) {
        writeShape(dataOutput, s);
      } else {
        boolean written = writeShapeByTypeIfSupported(dataOutput, s, type);
        if (!written)
          throw new IllegalArgumentException("Unsupported shape type "+s.getClass());
      }
    }
  }

}
