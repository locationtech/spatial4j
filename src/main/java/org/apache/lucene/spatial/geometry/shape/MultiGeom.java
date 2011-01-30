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

package org.apache.lucene.spatial.geometry.shape;

import java.util.Collection;

/**
 * A collection of Geometry2D objects.
 */
public class MultiGeom implements Geometry2D {
  private final Collection<Geometry2D> geoms;

  public MultiGeom(Collection<Geometry2D> geoms) {
    this.geoms = geoms;
  }

  @Override
  public void translate(Vector2D v) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public boolean contains(Point2D p) {
    for (Geometry2D geom : geoms) {
      if (geom.contains(p))
        return true;
    }
    return false;
  }

  @Override
  public double area() {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Point2D centroid() {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Rectangle boundingRectangle() {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;
    for (Geometry2D geom : geoms) {
      Rectangle r = geom.boundingRectangle();
      minX = Math.min(minX,r.getMinX());
      minY = Math.min(minY,r.getMinY());
      maxX = Math.max(maxX,r.getMaxX());
      maxY = Math.max(maxY,r.getMaxY());
    }
    return new Rectangle(minX,minY,maxX,maxY);
  }

  @Override
  public IntersectCase intersect(Rectangle r) {
    boolean allOutside = true;
    boolean allContains = true;
    for (Geometry2D geom : geoms) {
      IntersectCase sect = geom.intersect(r);
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
