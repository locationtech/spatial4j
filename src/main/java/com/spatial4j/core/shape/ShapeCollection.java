/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.impl.Range;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;

/**
 * A collection of Shape objects, analogous to an OGC GeometryCollection. The
 * implementation demands a List (with random access) so that the order can be
 * retained if an application requires it, although logically it's treated as an
 * unordered Set.  Consequently, {@link #relate(Shape)} should return the same
 * result no matter what the shape order is. There is no restriction on whether
 * the shapes overlap each other at all. As the Shape contract states; it may
 * return intersects when the best answer is actually contains or within. If
 * any shape intersects the provided shape then that is the answer.
 */
public class ShapeCollection<S extends Shape> extends AbstractList<S> implements Shape {

  //TODO add option to detect that all shapes are mutually disjoint. If that is
  // the case then relate() can return immediately if it finds a CONTAINS.

  protected final List<S> shapes;
  protected final Rectangle bbox;

  /**
   * WARNING: {@code shapes} is copied by reference.
   * @param shapes Copied by reference! (make a defensive copy if caller modifies)
   * @param ctx
   */
  public ShapeCollection(List<S> shapes, SpatialContext ctx) {
    if (shapes.isEmpty())
      throw new IllegalArgumentException("must be given at least 1 shape");
    if (!(shapes instanceof RandomAccess))
      throw new IllegalArgumentException("Shapes arg must implement RandomAccess: "+shapes.getClass());
    this.shapes = shapes;
    this.bbox = computeBoundingBox(shapes, ctx);
  }

  protected Rectangle computeBoundingBox(Collection<? extends Shape> shapes, SpatialContext ctx) {
    Range xRange = null;
    double minY = Double.POSITIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;
    for (Shape geom : shapes) {
      Rectangle r = geom.getBoundingBox();

      Range xRange2 = Range.xRange(r, ctx);
      if (xRange == null) {
        xRange = xRange2;
      } else {
        xRange = xRange.expandTo(xRange2);
      }
      minY = Math.min(minY, r.getMinY());
      maxY = Math.max(maxY, r.getMaxY());
    }
    return ctx.makeRectangle(xRange.getMin(), xRange.getMax(), minY, maxY);
  }

  public List<S> getShapes() {
    return shapes;
  }

  @Override
  public S get(int index) {
    return shapes.get(index);
  }

  @Override
  public int size() {
    return shapes.size();
  }

  @Override
  public Rectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public Point getCenter() {
    return bbox.getCenter();
  }

  @Override
  public boolean hasArea() {
    for (Shape geom : shapes) {
      if( geom.hasArea() ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public SpatialRelation relate(Shape other) {
    final SpatialRelation bboxSect = bbox.relate(other);
    if (bboxSect == SpatialRelation.DISJOINT || bboxSect == SpatialRelation.WITHIN)
      return bboxSect;

    SpatialRelation sect = null;//CONTAINS, WITHIN, or DISJOINT
    for (Shape shape : shapes) {
      SpatialRelation nextSect = shape.relate(other);

      if (sect == null) {//first pass
        sect = nextSect;
      } else {
        sect = sect.combine(nextSect);
      }

      if (sect == INTERSECTS)
        return INTERSECTS;
      if (sect == CONTAINS) {
        if (other instanceof Point)
          return CONTAINS;
        //TODO if mutually disjoint (cached in a bool flag), return CONTAINS
      }
    }
    return sect;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    double MAX_AREA = bbox.getArea(ctx);
    double sum = 0;
    for (Shape geom : shapes) {
      sum += geom.getArea(ctx);
      if (sum >= MAX_AREA)
        return MAX_AREA;
    }

    return sum;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(100);
    buf.append("ShapeCollection(");
    int i = 0;
    for (Shape shape : shapes) {
      if (i++ > 0)
        buf.append(", ");
      buf.append(shape);
      if (buf.length() > 150) {
        buf.append(" ...").append(shapes.size());
        break;
      }
    }
    buf.append(")");
    return buf.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShapeCollection that = (ShapeCollection) o;

    if (!shapes.equals(that.shapes)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return shapes.hashCode();
  }

}
