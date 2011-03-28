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

package org.apache.lucene.spatial.base.shape.jts;


import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Point;

public class JtsPoint2D implements org.apache.lucene.spatial.base.shape.Point {

  private Point point;

  public JtsPoint2D(Point point) {
    this.point = point;
  }

  public Point getPoint() {
    return point;
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
  public IntersectCase intersect(Shape other, Object context) {
    if(BBox.class.isInstance(other)) {
      BBox ext = other.getBoundingBox();
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
}
