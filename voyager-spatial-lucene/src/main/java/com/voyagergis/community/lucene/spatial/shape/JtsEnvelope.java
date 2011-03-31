/**
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

package com.voyagergis.community.lucene.spatial.shape;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JtsEnvelope implements BBox {

  public final Envelope envelope;

  public JtsEnvelope(Envelope envelope) {
    this.envelope = envelope;
  }

  public JtsEnvelope(double x1, double x2, double y1, double y2) {
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
    return envelope.getWidth();
  }

  @Override
  public double getWidth() {
    return envelope.getHeight();
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

  @Override
  public boolean hasSize() {
    return !envelope.isNull();
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public BBox getBoundingBox() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext context) {
    if (BBox.class.isInstance(other)) {
      BBox ext = other.getBoundingBox();
      if (ext.getMinX() > envelope.getMaxX() ||
          ext.getMaxX() < envelope.getMinX() ||
          ext.getMinY() > envelope.getMaxY() ||
          ext.getMaxY() < envelope.getMinY()) {
        return IntersectCase.OUTSIDE;
      }

      if (ext.getMinX() >= envelope.getMinX() &&
          ext.getMaxX() <= envelope.getMaxX() &&
          ext.getMinY() >= envelope.getMinY() &&
          ext.getMaxY() <= envelope.getMaxY()) {
        return IntersectCase.CONTAINS;
      }

      if (envelope.getMinX() >= ext.getMinY() &&
          envelope.getMaxX() <= ext.getMaxX() &&
          envelope.getMinY() >= ext.getMinY() &&
          envelope.getMaxY() <= ext.getMaxY()) {
        return IntersectCase.WITHIN;
      }
      return IntersectCase.INTERSECTS;
    } else if (JtsGeometry.class.isInstance(other)) {
      throw new IllegalArgumentException("TODO...");
    }
    throw new IllegalArgumentException("JtsEnvelope can be compared with Envelope or Geogmetry");
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
  public JtsPoint2D getCentroid() {
    return new JtsPoint2D(new GeometryFactory().createPoint(envelope.centre()));
  }
}
