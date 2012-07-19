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

package com.spatial4j.core.shape;

/**
 * The set of spatial relationships.  Naming is consistent with OGC spec
 * conventions as seen in SQL/MM and others.
 * <p/>
 * There is no equality case.  If two Shape instances are equal then the result
 * might be CONTAINS (preferred) or WITHIN.  Client logic may have to be aware
 * of this edge condition; Spatial4j testing certainly does.
 */
public enum SpatialRelation {
  WITHIN,
  CONTAINS,
  DISJOINT,
  INTERSECTS;
  //Don't have these: TOUCHES, CROSSES, OVERLAPS

  /**
   * Given the result of <code>shapeA.relate(shapeB)</code>, transposing that
   * result should yield the result of <code>shapeB.relate(shapeA)</code>. There
   * is a corner case is when the shapes are equal, in which case actually
   * flipping the relate() call will result in the same value -- either CONTAINS
   * or WITHIN.
   */
  public SpatialRelation transpose() {
    switch(this) {
      case CONTAINS: return SpatialRelation.WITHIN;
      case WITHIN: return SpatialRelation.CONTAINS;
      default: return this;
    }
  }

//  /**
//   * If you were to call aShape.relate(bShape) and aShape.relate(cShape), you
//   * could call this to merge the intersect results as if bShape & cShape were
//   * combined into {@link MultiShape}.
//   */
//  SpatialRelation combine(SpatialRelation other) {
//    if (this == other)
//      return this;
//    if (this == WITHIN || other == WITHIN)
//      return WITHIN;
//    return INTERSECTS;
//  }

  /** Not DISJOINT, i.e. there is some sort of intersection. */
  public boolean intersects() {
    return this != DISJOINT;
  }

  /**
   * If <code>aShape.relate(bShape)</code> is r, then <code>r.inverse()</code>
   * is <code> inverse(aShape).relate(bShape)</code> whereas
   * <code>inverse(shape)</code> is theoretically the opposite area covered by a
   * shape, i.e. everywhere but where the shape is.
   * <p/>
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
