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

package com.googlecode.lucene.spatial.base.shape;


import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Point;

public class JtsPoint implements org.apache.lucene.spatial.base.shape.Point {

  private Point point;

  public JtsPoint(Point point) {
    this.point = point;
  }

  public Point getJtsPoint() {
    return point;
  }

  @Override
  public org.apache.lucene.spatial.base.shape.Point getCenter() {
    return this;
  }

  @Override
  public boolean hasArea() {
    return false;
  }

  @Override
  public JtsEnvelope getBoundingBox() {
    return new JtsEnvelope(point.getEnvelopeInternal());
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialContext context) {
    if(Rectangle.class.isInstance(other)) {
      Rectangle ext = other.getBoundingBox();
      if (point.getX() >= ext.getMinX() &&
          point.getX() <= ext.getMaxX() &&
          point.getY() >= ext.getMinY() &&
          point.getY() <= ext.getMaxY()) {
        return IntersectCase.WITHIN;
      }
      return IntersectCase.OUTSIDE;
    } else if(JtsGeometry.class.isInstance(other)) {
      if (((JtsGeometry)other).geo.contains(point)) {
        return IntersectCase.WITHIN;
      }
      return IntersectCase.OUTSIDE;
    }
    throw new IllegalArgumentException( "JtsEnvelope can be compared with Envelope or Geogmetry" );
  }

  @Override
  public double getX() {
    return point.getX();
  }

  @Override
  public double getY() {
    return point.getY();
  }
  @Override
  public String toString() {
    return "Pt(x="+getX()+",y="+getY()+")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JtsPoint that = (JtsPoint) o;
    return point.equals(that.point);
  }

  @Override
  public int hashCode() {
    return point.hashCode();
  }
}
