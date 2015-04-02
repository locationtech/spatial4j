/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Ryan McKinley - initial API and implementation
 *    David Smiley
 ******************************************************************************/

package org.locationtech.spatial4j.shape.jts;


import com.vividsolutions.jts.geom.CoordinateSequence;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.locationtech.spatial4j.shape.impl.PointImpl;

/** Wraps a {@link com.vividsolutions.jts.geom.Point}. */
public class JtsPoint implements Point {

  private final SpatialContext ctx;
  private com.vividsolutions.jts.geom.Point pointGeom;
  private final boolean empty;//cached

  /** A simple constructor without normalization / validation. */
  public JtsPoint(com.vividsolutions.jts.geom.Point pointGeom, SpatialContext ctx) {
    this.ctx = ctx;
    this.pointGeom = pointGeom;
    this.empty = pointGeom.isEmpty();
  }

  public com.vividsolutions.jts.geom.Point getGeom() {
    return pointGeom;
  }

  @Override
  public boolean isEmpty() {
    return empty;
  }

  @Override
  public org.locationtech.spatial4j.shape.Point getCenter() {
    return this;
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
  public Rectangle getBoundingBox() {
    return ctx.makeRectangle(this, this);
  }

  @Override
  public Circle getBuffered(double distance, SpatialContext ctx) {
    return ctx.makeCircle(this, distance);
  }

  @Override
  public SpatialRelation relate(Shape other) {
    // ** NOTE ** the overall order of logic is kept consistent here with simple.PointImpl.
    if (isEmpty() || other.isEmpty())
      return SpatialRelation.DISJOINT;
    if (other instanceof org.locationtech.spatial4j.shape.Point)
      return this.equals(other) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
    return other.relate(this).transpose();
  }

  @Override
  public double getX() {
    return isEmpty() ? Double.NaN : pointGeom.getX();
  }

  @Override
  public double getY() {
    return isEmpty() ? Double.NaN : pointGeom.getY();
  }

  @Override
  public void reset(double x, double y) {
    assert ! isEmpty();
    CoordinateSequence cSeq = pointGeom.getCoordinateSequence();
    cSeq.setOrdinate(0, CoordinateSequence.X, x);
    cSeq.setOrdinate(0, CoordinateSequence.Y, y);
  }

  @Override
  public String toString() {
    return "Pt(x="+getX()+",y="+getY()+")";
  }

  @Override
  public boolean equals(Object o) {
    return PointImpl.equals(this,o);
  }

  @Override
  public int hashCode() {
    return PointImpl.hashCode(this);
  }
}
