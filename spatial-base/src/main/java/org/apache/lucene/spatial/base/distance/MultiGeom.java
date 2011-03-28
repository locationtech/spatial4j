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

package org.apache.lucene.spatial.base.distance;

import java.util.Collection;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;

/**
 * A collection of Geometry2D objects.
 */
public class MultiGeom implements Shape {
  private final Collection<Shape> geoms;

  public MultiGeom(Collection<Shape> geoms) {
    this.geoms = geoms;
  }

  @Override
  public BBox getBoundingBox() {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (Shape geom : geoms) {
      BBox r = geom.getBoundingBox();
      minX = Math.min(minX,r.getMinX());
      minY = Math.min(minY,r.getMinY());
      maxX = Math.max(maxX,r.getMaxX());
      maxY = Math.max(maxY,r.getMaxY());
    }
    return new Rectangle(minX,minY,maxX,maxY);
  }

  @Override
  public boolean hasArea() {
    for (Shape geom : geoms) {
      if( geom.hasArea() ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IntersectCase intersect(Shape other, Object context) {
    boolean allOutside = true;
    boolean allContains = true;
    for (Shape geom : geoms) {
      IntersectCase sect = geom.intersect(other, context);
      if (sect != IntersectCase.OUTSIDE)
        allOutside = false;
      if (sect != IntersectCase.CONTAINS)
        allContains = false;
      if (!allContains && !allOutside)
        return IntersectCase.INTERSECTS;//short circuit
    }
    if (allOutside)
      return IntersectCase.OUTSIDE;
    if (allContains)
      return IntersectCase.CONTAINS;
    return IntersectCase.INTERSECTS;
  }
}
