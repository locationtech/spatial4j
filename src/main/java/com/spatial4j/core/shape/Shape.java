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

/**
 * The base interface defining a geometric shape. Shape instances should be
 * instantiated via one of the create* methods on a {@link SpatialContext} or
 * by reading WKT which calls those methods; they should <em>not</em> be
 * created directly.
 * <p/>
 * Shapes are generally immutable and thread-safe. If a particular shape has a
 * <code>reset(...)</code> method then its use means the shape is actually
 * mutable. Mutating shape state is considered expert and should be done with care.
 */
public interface Shape {

  /**
   * Describe the relationship between the two objects.  For example
   * <ul>
   *   <li>this is WITHIN other</li>
   *   <li>this CONTAINS other</li>
   *   <li>this is DISJOINT other</li>
   *   <li>this INTERSECTS other</li>
   * </ul>
   * Note that a Shape implementation may choose to return INTERSECTS when the
   * true answer is WITHIN or CONTAINS for performance reasons. If a shape does
   * this then it <i>must</i> document when it does.  Ideally the shape will not
   * do this approximation in all circumstances, just sometimes.
   * <p />
   * If the shapes are equal then the result is CONTAINS (preferred) or WITHIN.
   */
  SpatialRelation relate(Shape other);

  /**
   * Get the bounding box for this Shape. This means the shape is within the
   * bounding box and that it touches each side of the rectangle.
   * <p />
   * Postcondition: <code>this.getBoundingBox().relate(this) == CONTAINS</code>
   */
  Rectangle getBoundingBox();

  /**
   * Does the shape have area?  This will be false for points and lines. It will
   * also be false for shapes that normally have area but are constructed in a
   * degenerate case as to not have area (e.g. a circle with 0 radius or
   * rectangle with no height or no width).
   */
  boolean hasArea();

  /**
   * Calculates the area of the shape, in square-degrees. If ctx is null then
   * simple Euclidean calculations will be used.  This figure can be an
   * estimate.
   */
  double getArea(SpatialContext ctx);

  /**
   * Returns the center point of this shape. This is usually the same as
   * <code>getBoundingBox().getCenter()</code> but it doesn't have to be.
   * <p />
   * Postcondition: <code>this.relate(this.getCenter()) == CONTAINS</code>
   */
  Point getCenter();

  /**
   * Returns a buffered version of this shape.  The buffer is usually a
   * rounded-corner buffer, although some shapes might buffer differently. This
   * is an optional operation.
   *
   *
   * @param distance
   * @return Not null, and the returned shape should contain the current shape.
   */
  Shape getBuffered(double distance, SpatialContext ctx);

  /**
   * Shapes can be "empty", which is to say it exists nowhere. The underlying coordinates are
   * typically NaN.
   */
  boolean isEmpty();

  /** The sub-classes of Shape generally implement the
   * same contract for {@link Object#equals(Object)} and {@link Object#hashCode()}
   * amongst the same sub-interface type.  This means, for example, that multiple
   * Point implementations of different classes are equal if they share the same x
   * & y. */
  @Override
  public boolean equals(Object other);
}

