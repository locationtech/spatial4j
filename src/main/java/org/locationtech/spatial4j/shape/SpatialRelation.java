/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

/**
 * The set of spatial relationships.  Naming is somewhat consistent with OGC spec
 * conventions as seen in SQL/MM and others.
 * <p>
 * There is no equality case.  If two Shape instances are equal then the result
 * might be CONTAINS (preferred) or WITHIN.  Client logic may have to be aware
 * of this edge condition; Spatial4j testing certainly does.
 * <p>
 * The "CONTAINS" and "WITHIN" wording here is inconsistent with OGC; these here map to OGC
 * "COVERS" and "COVERED BY", respectively. The distinction is in the boundaries; in Spatial4j
 * there is no boundary distinction -- boundaries are part of the shape as if it was an "interior",
 * with respect to OGC's terminology.
 */
public enum SpatialRelation {
  //see http://docs.geotools.org/latest/userguide/library/jts/dim9.html#preparedgeometry

  /**
   * The shape is within the target geometry. It's the converse of {@link #CONTAINS}.
   * Boundaries of shapes count too.  OGC specs refer to this relation as "COVERED BY";
   * WITHIN is differentiated there by not including boundaries.
   */
  WITHIN,

  /**
   * The shape contains the target geometry. It's the converse of {@link #WITHIN}.
   * Boundaries of shapes count too.  OGC specs refer to this relation as "COVERS";
   * CONTAINS is differentiated there by not including boundaries.
   */
  CONTAINS,

  /**
   * The shape shares no point in common with the target shape.
   */
  DISJOINT,

  /**
   * The shape shares some points/overlap with the target shape, and the relation is
   * not more specifically {@link #WITHIN} or {@link #CONTAINS}.
   */
  INTERSECTS;
  //Don't have these: TOUCHES, CROSSES, OVERLAPS, nor distinction between CONTAINS/COVERS

  /**
   * Given the result of <code>shapeA.relate(shapeB)</code>, transposing that
   * result should yield the result of <code>shapeB.relate(shapeA)</code>. There
   * is a corner case is when the shapes are equal, in which case actually
   * flipping the relate() call will result in the same value -- either CONTAINS
   * or WITHIN; this method can't possible check for that so the caller might
   * have to.
   */
  public SpatialRelation transpose() {
    switch(this) {
      case CONTAINS: return SpatialRelation.WITHIN;
      case WITHIN: return SpatialRelation.CONTAINS;
      default: return this;
    }
  }

  /**
   * If you were to call aShape.relate(bShape) and aShape.relate(cShape), you
   * could call this to merge the intersect results as if bShape &amp; cShape were
   * combined into {@link ShapeCollection}.  If {@code other} is null then the
   * result is "this".
   */
  public SpatialRelation combine(SpatialRelation other) {
    // You can think of this algorithm as a state transition / automata.
    // 1. The answer must be the same no matter what the order is.
    // 2. If any INTERSECTS, then the result is INTERSECTS (done).
    // 3. A DISJOINT + WITHIN == INTERSECTS (done).
    // 4. A DISJOINT + CONTAINS == CONTAINS.
    // 5. A CONTAINS + WITHIN == INTERSECTS (done). (weird scenario)
    // 6. X + X == X.)
    // 7. X + null == X;
    if (other == this || other == null)
      return this;
    if (this == DISJOINT && other == CONTAINS
        || this == CONTAINS && other == DISJOINT)
      return CONTAINS;
    return INTERSECTS;
  }

  /** Not DISJOINT, i.e. there is some sort of intersection. */
  public boolean intersects() {
    return this != DISJOINT;
  }

  /**
   * If <code>aShape.relate(bShape)</code> is r, then <code>r.inverse()</code>
   * is <code> inverse(aShape).relate(bShape)</code> whereas
   * <code>inverse(shape)</code> is theoretically the opposite area covered by a
   * shape, i.e. everywhere but where the shape is.
   * <p>
   * Note that it's not commutative!  <code>WITHIN.inverse().inverse() !=
   * WITHIN</code>.
   */
  public SpatialRelation inverse() {
    switch(this) {
      case DISJOINT: return CONTAINS;
      case CONTAINS: return DISJOINT;
      case WITHIN: return INTERSECTS;//not commutative!
    }
    return INTERSECTS;
  }

}
