/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.impl;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.BaseShape;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.SpatialRelation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A BufferedLineString is a collection of {@link org.locationtech.spatial4j.shape.impl.BufferedLine} shapes,
 * resulting in what some call a "Track" or "Polyline" (ESRI terminology).
 * The buffer can be 0.  Note that BufferedLine isn't yet aware of geodesics (e.g. the anti-meridian).
 */
public class BufferedLineString extends BaseShape<SpatialContext> {

  //TODO add some geospatial awareness like:
  // segment that spans at the dateline (split it at DL?).

  private final ShapeCollection<BufferedLine> segments;
  private final double buf;

  /**
   * Needs at least 1 point, usually more than that.  If just one then it's
   * internally treated like 2 points.
   */
  public BufferedLineString(List<Point> points, double buf, SpatialContext ctx) {
    this(points, buf, false, ctx);
  }

  /**
   * @param points ordered control points. If empty then this shape is empty.
   * @param buf Buffer &gt;= 0
   * @param expandBufForLongitudeSkew See {@link BufferedLine
   * #expandBufForLongitudeSkew(org.locationtech.spatial4j.shape.Point,
   * org.locationtech.spatial4j.shape.Point, double)}.
   *                                  If true then the buffer for each segment
   *                                  is computed.
   * @param ctx
   */
  public BufferedLineString(List<Point> points, double buf, boolean expandBufForLongitudeSkew,
                            SpatialContext ctx) {
    super(ctx);
    this.buf = buf;

    if (points.isEmpty()) {
      this.segments = ctx.makeCollection(Collections.<BufferedLine>emptyList());
    } else {
      List<BufferedLine> segments = new ArrayList<BufferedLine>(points.size() - 1);

      Point prevPoint = null;
      for (Point point : points) {
        if (prevPoint != null) {
          double segBuf = buf;
          if (expandBufForLongitudeSkew) {
            //TODO this is faulty in that it over-buffers.  See Issue#60.
            segBuf = BufferedLine.expandBufForLongitudeSkew(prevPoint, point, buf);
          }
          segments.add(new BufferedLine(prevPoint, point, segBuf, ctx));
        }
        prevPoint = point;
      }
      if (segments.isEmpty()) {//TODO throw exception instead?
        segments.add(new BufferedLine(prevPoint, prevPoint, buf, ctx));
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

  public ShapeCollection<BufferedLine> getSegments() {
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
    final List<BufferedLine> lines = segments.getShapes();
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

    BufferedLineString that = (BufferedLineString) o;

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
}
