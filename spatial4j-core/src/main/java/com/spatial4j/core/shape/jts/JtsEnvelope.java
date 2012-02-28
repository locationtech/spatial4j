/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.jts;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

import com.vividsolutions.jts.geom.Envelope;

public class JtsEnvelope implements Rectangle {

  public final Envelope envelope;

  public JtsEnvelope(Envelope envelope) {
    this.envelope = envelope;
  }

  public JtsEnvelope(double x1, double x2, double y1, double y2) {
    if (x2 < x1)
      throw new IllegalArgumentException("JtsEnvelope doesn't support crossing the dateline.");
    this.envelope = new Envelope(x1, x2, y1, y2);
  }

  @Override
  public boolean hasArea() {
    return getWidth() > 0 && getHeight() > 0;
  }

  public double getArea() {
    return getWidth() * getHeight();
  }

  public boolean getCrossesDateLine() {
    return false;
  }

  @Override
  public double getHeight() {
    return envelope.getHeight();
  }

  @Override
  public double getWidth() {
    return envelope.getWidth();
  }

  @Override
  public double getMaxX() {
    return envelope.getMaxX();
  }

  @Override
  public double getMaxY() {
    return envelope.getMaxY();
  }

  @Override
  public double getMinX() {
    return envelope.getMinX();
  }

  @Override
  public double getMinY() {
    return envelope.getMinY();
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public JtsEnvelope getBoundingBox() {
    return this;
  }

  @Override
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    // ** NOTE ** the overall order of logic is kept consistent here with simple.RectangleImpl.
    // ... except this doesn't do date-line cross
    if (other instanceof Point) {
      Point p = (Point)other;
      return (envelope.contains(p.getX(),p.getY())) ? SpatialRelation.CONTAINS : SpatialRelation.DISJOINT;
    }

    if (! (other instanceof Rectangle) ) {
      return other.relate(this, ctx).transpose();
    }

    // Rectangle...
    Rectangle ext = other.getBoundingBox();
    if (ext.getMinX() > envelope.getMaxX() ||
        ext.getMaxX() < envelope.getMinX() ||
        ext.getMinY() > envelope.getMaxY() ||
        ext.getMaxY() < envelope.getMinY()) {
      return SpatialRelation.DISJOINT;
    }

    if (ext.getMinX() >= envelope.getMinX() &&
        ext.getMaxX() <= envelope.getMaxX() &&
        ext.getMinY() >= envelope.getMinY() &&
        ext.getMaxY() <= envelope.getMaxY()) {
      return SpatialRelation.CONTAINS;
    }

    if (envelope.getMinX() >= ext.getMinX() &&
        envelope.getMaxX() <= ext.getMaxX() &&
        envelope.getMinY() >= ext.getMinY() &&
        envelope.getMaxY() <= ext.getMaxY()) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public SpatialRelation relate_yRange(double minY, double maxY, SpatialContext ctx) {
    if (minY > envelope.getMaxY() ||
        maxY < envelope.getMinY()) {
      return SpatialRelation.DISJOINT;
    }

    if (minY >= envelope.getMinY() &&
        maxY <= envelope.getMaxY()) {
      return SpatialRelation.CONTAINS;
    }

    if ( envelope.getMinY() >= minY &&
        envelope.getMaxY() <= maxY) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public SpatialRelation relate_xRange(double minX, double maxX, SpatialContext ctx) {
    if (minX > envelope.getMaxX() ||
        maxX < envelope.getMinX()) {
      return SpatialRelation.DISJOINT;
    }

    if (minX >= envelope.getMinX() &&
        maxX <= envelope.getMaxX()) {
      return SpatialRelation.CONTAINS;
    }

    if ( envelope.getMinX() >= minX &&
        envelope.getMaxX() <= maxX) {
      return SpatialRelation.WITHIN;
    }
    return SpatialRelation.INTERSECTS;
  }

  @Override
  public String toString() {
    return envelope.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JtsEnvelope that = (JtsEnvelope) o;
    return envelope.equals(that.envelope);
  }

  @Override
  public int hashCode() {
    return envelope.hashCode();
  }

  @Override
  public Point getCenter() {
    //TODO make JtsCoordinate (more lightweight than JtsPoint) ?
//    final Coordinate centre = envelope.centre();
//    return new PointImpl(centre.x, centre.y);
    return new JtsPoint(new GeometryFactory().createPoint(envelope.centre()));
  }
}
