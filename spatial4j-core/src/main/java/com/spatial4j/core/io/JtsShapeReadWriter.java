package com.spatial4j.core.io;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JtsShapeReadWriter extends ShapeReadWriter<JtsSpatialContext> {

  private static final byte TYPE_POINT = 0;
  private static final byte TYPE_BBOX = 1;
  private static final byte TYPE_GEOM = 2;

  public JtsShapeReadWriter(JtsSpatialContext ctx) {
    super(ctx);
  }

  private void normalizeCoordinates(Geometry geom) {
    //TODO add configurable skip flag if input is in the right coordinates
    if (!ctx.isGeo())
      return;
    geom.apply(new CoordinateSequenceFilter() {
      boolean changed = false;
      @Override
      public void filter(CoordinateSequence seq, int i) {
        double x = seq.getX(i);
        double xNorm = ctx.normX(x);
        if (x != xNorm) {
          changed = true;
          seq.setOrdinate(i,CoordinateSequence.X,xNorm);
        }
        double y = seq.getY(i);
        double yNorm = ctx.normY(y);
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

  /**
   * Reads the standard shape format + WKT.
   */
  @Override
  public Shape readShape(String str) throws InvalidShapeException {
    Shape shape = super.readStandardShape(str);
    if( shape == null ) {
      try {
        WKTReader reader = new WKTReader(ctx.getGeometryFactory());
        Geometry geom = reader.read(str);

        //Normalize coordinates to geo boundary
        normalizeCoordinates(geom);

        if (geom instanceof com.vividsolutions.jts.geom.Point) {
          return new JtsPoint((com.vividsolutions.jts.geom.Point)geom);
        } else if (geom.isRectangle()) {
          boolean crossesDateline = false;
          if (ctx.isGeo()) {
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
        return new JtsGeometry(geom,ctx);
      } catch(com.vividsolutions.jts.io.ParseException ex) {
        throw new InvalidShapeException("error reading WKT", ex);
      }
    }
    return shape;
  }

  @Override
  public String writeShape(Shape shape) {
    if (shape instanceof JtsGeometry) {
      JtsGeometry jtsGeom = (JtsGeometry) shape;
      return jtsGeom.getGeom().toText();
    }
    return super.writeShape(shape);
  }

  /**
   * Reads a shape from a byte array, using an internal format written by
   * {@link #writeShapeToBytes(com.spatial4j.core.shape.Shape)}.
   */
  public Shape readShapeFromBytes(final byte[] array, final int offset, final int length) throws InvalidShapeException {
    ByteBuffer bytes = ByteBuffer.wrap(array, offset, length);
    byte type = bytes.get();
    if (type == TYPE_POINT) {
      return new JtsPoint(ctx.getGeometryFactory().createPoint(new Coordinate(bytes.getDouble(), bytes.getDouble())));
    } else if (type == TYPE_BBOX) {
      return new RectangleImpl(
              bytes.getDouble(), bytes.getDouble(),
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
        normalizeCoordinates(geom);
        return new JtsGeometry(geom, ctx);
      } catch(ParseException ex) {
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
