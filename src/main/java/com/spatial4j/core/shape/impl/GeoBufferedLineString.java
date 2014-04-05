package com.spatial4j.core.shape.impl;


import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeoBufferedLineString implements Shape {

  private final ShapeCollection<GeoBufferedLine> segments;
  private final double buf;
  /**
   * Needs at least 1 point, usually more than that.  If just one then it's
   * internally treated like 2 points.
   */
  public GeoBufferedLineString(List<Point> points, double buf, SpatialContext ctx) {
    this(points, buf, false, ctx);
  }

  /**
   * @param points ordered control points. If empty then this shape is empty.
   * @param buf Buffer >= 0
   * @param expandBufForLongitudeSkew See {@link GeoBufferedLine
   * #expandBufForLongitudeSkew(com.spatial4j.core.shape.Point,
   * com.spatial4j.core.shape.Point, double)}.
   *                                  If true then the buffer for each segment
   *                                  is computed.
   * @param ctx
   */
  public GeoBufferedLineString(List<Point> points, double buf, boolean expandBufForLongitudeSkew,
                            SpatialContext ctx) {
    this.buf = buf;

    if (points.isEmpty()) {
      this.segments = ctx.makeCollection(Collections.<GeoBufferedLine>emptyList());
    } else {
      List<GeoBufferedLine> segments = new ArrayList<GeoBufferedLine>(points.size() - 1);

      Point prevPoint = null;
      for (Point point : points) {
        if (prevPoint != null) {
          double segBuf = buf;
          segments.add(new GeoBufferedLine(prevPoint, point, segBuf, ctx));
        }
        prevPoint = point;
      }
      if (segments.isEmpty()) {//TODO throw exception instead?
        segments.add(new GeoBufferedLine(prevPoint, prevPoint, buf, ctx));
      }
      this.segments = ctx.makeCollection(segments);
    }
  }

  @Override
  public boolean isEmpty() {
    return segments.isEmpty();
  }

  @Override
  public Shape getBuffered(double distance, SpatialContext ctx) {
    return ctx.makeBufferedLineString(getPoints(), buf + distance);
  }

  public ShapeCollection<GeoBufferedLine> getSegments() {
    return segments;
  }

  public double getBuf() {
    return buf;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return segments.getArea(ctx);
  }

  @Override
  public SpatialRelation relate(Shape other) {
    return segments.relate(other);
  }

  @Override
  public boolean hasArea() {
    return segments.hasArea();
  }

  @Override
  public Point getCenter() {
    return segments.getCenter();
  }

  @Override
  public Rectangle getBoundingBox() {
    return segments.getBoundingBox();
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder(100);
    str.append("BufferedLineString(buf=").append(buf).append(" pts=");
    boolean first = true;
    for (Point point : getPoints()) {
      if (first) {
        first = false;
      } else {
        str.append(", ");
      }
      str.append(point.getX()).append(' ').append(point.getY());
    }
    str.append(')');
    return str.toString();
  }

  public List<Point> getPoints() {
    if (segments.isEmpty())
      return Collections.emptyList();
    final List<GeoBufferedLine> lines = segments.getShapes();
    return new AbstractList<Point>() {
      @Override
      public Point get(int index) {
        if (index == 0)
          return lines.get(0).getA();
        return lines.get(index - 1).getB();
      }

      @Override
      public int size() {
        return lines.size() + 1;
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

   GeoBufferedLineString that = (GeoBufferedLineString) o;

    if (Double.compare(that.buf, buf) != 0) return false;
    if (!segments.equals(that.segments)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = segments.hashCode();
    temp = buf != +0.0d ? Double.doubleToLongBits(buf) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  static class PairSegment extends ShapeCollection<GeoBufferedLine> implements LineSegment {

    /**
     * WARNING: {@code shapes} is copied by reference.
     *
     * @param shapes Copied by reference! (make a defensive copy if caller modifies)
     * @param ctx
     */
    public PairSegment(List<GeoBufferedLine> shapes, SpatialContext ctx) {
      super(shapes, ctx);
    }

    @Override
    public Point getA() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Point getB() {
      throw new UnsupportedOperationException();
    }
  }
}
