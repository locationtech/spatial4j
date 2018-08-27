/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.impl;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.BaseShape;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;

/** A basic 2D implementation of a Point. */
public class PointImpl extends BaseShape<SpatialContext> implements Point {

  private double x;
  private double y;

  /** A simple constructor without normalization / validation. */
  public PointImpl(double x, double y, SpatialContext ctx) {
    super(ctx);
    reset(x, y);
  }

  @Override
  public boolean isEmpty() {
    return Double.isNaN(x);
  }

  @Override
  public void reset(double x, double y) {
    assert ! isEmpty();
    this.x = x;
    this.y = y;
  }

  @Override
  public double getX() {
    return x;
  }

  @Override
  public double getY() {
    return y;
  }

  @Override
  public double getLat() {
    return getY();
  }

  @Override
  public double getLon() {
    return getX();
  }

  @Override
  public Rectangle getBoundingBox() {
    return ctx.makeRectangle(this, this);
  }

  @Override
  public PointImpl getCenter() {
    return this;
  }

  @Override
  public Circle getBuffered(double distance, SpatialContext ctx) {
    return ctx.makeCircle(this, distance);
  }

  @Override
  public SpatialRelation relate(Shape other) {
    if (isEmpty() || other.isEmpty())
      return SpatialRelation.DISJOINT;
    if (other instanceof Point)
      return this.equals(other) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
    return other.relate(this).transpose();
  }

  @Override
  public boolean hasArea() {
    return false;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return 0;
  }

  @Override
  public String toString() {
    return "Pt(x="+x+",y="+y+")";
  }

  @Override
  public boolean equals(Object o) {
    return equals(this,o);
  }

  /**
   * All {@link Point} implementations should use this definition of {@link Object#equals(Object)}.
   */
  public static boolean equals(Point thiz, Object o) {
    assert thiz != null;
    if (thiz == o) return true;
    if (!(o instanceof Point)) return false;

    Point point = (Point) o;

    if (Double.compare(point.getX(), thiz.getX()) != 0) return false;
    if (Double.compare(point.getY(), thiz.getY()) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return hashCode(this);
  }

  /**
   * All {@link Point} implementations should use this definition of {@link Object#hashCode()}.
   */
  public static int hashCode(Point thiz) {
    int result;
    long temp;
    temp = thiz.getX() != +0.0d ? Double.doubleToLongBits(thiz.getX()) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = thiz.getY() != +0.0d ? Double.doubleToLongBits(thiz.getY()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
