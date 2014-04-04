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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;

/**
 * A collection of Shape objects, analogous to an OGC GeometryCollection. The
 * implementation demands a List (with random access) so that the order can be
 * retained if an application requires it, although logically it's treated as an
 * unordered Set, mostly.
 * <p/>
 * Ideally, {@link #relate(Shape)} should return the same result no matter what
 * the shape order is, although the default implementation can be order
 * dependent when the shapes overlap; see {@link #relateContainsShortCircuits()}.
 *  To improve performance slightly, the caller could order the shapes by
 * largest first so that relate() will have a greater chance of
 * short-circuit'ing sooner.  As the Shape contract states; it may return
 * intersects when the best answer is actually contains or within. If any shape
 * intersects the provided shape then that is the answer.
 * <p/>
 * This implementation is not optimized for a large number of shapes; relate is
 * O(N).  A more sophisticated implementation might do an R-Tree based on
 * bbox'es, for example.
 */
public class ShapeCollection<S extends Shape> extends AbstractList<S> implements Shape {

  protected final List<S> shapes;
  protected final Rectangle bbox;

  /**
   * WARNING: {@code shapes} is copied by reference.
   * @param shapes Copied by reference! (make a defensive copy if caller modifies)
   * @param ctx
   */
  public ShapeCollection(List<S> shapes, SpatialContext ctx) {
    if (!(shapes instanceof RandomAccess))
      throw new IllegalArgumentException("Shapes arg must implement RandomAccess: "+shapes.getClass());
    this.shapes = shapes;
    this.bbox = computeBoundingBox(shapes, ctx);
  }

  protected Rectangle computeBoundingBox(Collection<? extends Shape> shapes, SpatialContext ctx) {
    if (shapes.isEmpty())
      return ctx.makeRectangle(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
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
  public ShapeCollection getBuffered(double distance, SpatialContext ctx) {
    List<Shape> bufColl = new ArrayList<Shape>(size());
    for (Shape shape : shapes) {
      bufColl.add(shape.getBuffered(distance, ctx));
    }
    return ctx.makeCollection(bufColl);
  }

  @Override
  public SpatialRelation relate(Shape other) {
    final SpatialRelation bboxSect = bbox.relate(other);
    if (bboxSect == SpatialRelation.DISJOINT || bboxSect == SpatialRelation.WITHIN)
      return bboxSect;

    final boolean containsWillShortCircuit = (other instanceof Point) ||
        relateContainsShortCircuits();
    SpatialRelation sect = null;
    for (Shape shape : shapes) {
      SpatialRelation nextSect = shape.relate(other);

      if (sect == null) {//first pass
        sect = nextSect;
      } else {
        sect = sect.combine(nextSect);
      }

      if (sect == INTERSECTS)
        return INTERSECTS;

      if (sect == CONTAINS && containsWillShortCircuit)
        return CONTAINS;
    }
    return sect;
  }

  /**
   * Called by relate() to determine whether to return early if it finds
   * CONTAINS, instead of checking the remaining shapes. It will do so without
   * calling this method if the "other" shape is a Point.  If a remaining shape
   * finds INTERSECTS, then INTERSECTS will be returned.  The only problem with
   * this returning true is that if some of the shapes overlap, it's possible
   * that the result of relate() could be dependent on the order of the shapes,
   * which could be unexpected / wrong depending on the application. The default
   * implementation returns true because it probably doesn't matter.  If it
   * does, a subclass could add a boolean flag that this method could return.
   * That flag could be initialized to true only if the shapes are mutually
   * disjoint.
   *
   * @see #computeMutualDisjoint(java.util.List) .
   */
  protected boolean relateContainsShortCircuits() {
    return true;
  }

  /**
   * Computes whether the shapes are mutually disjoint. This is a utility method
   * offered for use by a subclass implementing {@link #relateContainsShortCircuits()}.
   * <b>Beware: this is an O(N^2) algorithm.</b>.  Consequently, consider safely
   * assuming non-disjoint if shapes.size() > 10 or something.  And if all shapes
   * are a Point then the result of this method doesn't ultimately matter.
   */
  protected static boolean computeMutualDisjoint(List<? extends Shape> shapes) {
    //WARNING: this is an O(n^2) algorithm.
    //loop through each shape and see if it intersects any shape before it
    for (int i = 1; i < shapes.size(); i++) {
      Shape shapeI = shapes.get(i);
      for (int j = 0; j < i; j++) {
        Shape shapeJ = shapes.get(j);
        if (shapeJ.relate(shapeI).intersects())
          return false;
      }
    }
    return true;
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
