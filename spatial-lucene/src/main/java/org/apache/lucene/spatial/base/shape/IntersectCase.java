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

package org.apache.lucene.spatial.base.shape;

/**

 No equality case.  If two Shape instances are equal then the result might be CONTAINS or WITHIN, and
  some logic might fail under this edge condition when it's not careful to check.
  Client code must be written to detect this and act accordingly.  In RectangleImpl.intersect(), it checks
   for this explicitly, for example.  TestShapes2D.assertIntersect() checks too.
 */
public enum IntersectCase {
  WITHIN,
  CONTAINS,
  OUTSIDE,
  INTERSECTS;


  public IntersectCase transpose() {
    switch(this) {
      case CONTAINS: return IntersectCase.WITHIN;
      case WITHIN: return IntersectCase.CONTAINS;
      default: return this;
    }
  }

  /**
   * TODO need to test this!
   * If you were to call aShape.intersect(bShape) and aShape.intersect(cShape), you could call
   * this to merge the intersect results as if bShape & cShape were combined into {@link MultiShape}.
   * @param other
   * @return
   */
  public IntersectCase combine(IntersectCase other) {
    if (this == other)
      return this;
    if (this == WITHIN || other == WITHIN)
      return WITHIN;
    return INTERSECTS;
  }

  public boolean intersects() {
    return this != OUTSIDE;
  }

  /** Not commutative!  WITHIN.inverse().inverse() != WITHIN. */
  public IntersectCase inverse() {
    switch(this) {
      case OUTSIDE: return CONTAINS;
      case CONTAINS: return OUTSIDE;
      case WITHIN: return INTERSECTS;//not commutative!
    }
    return INTERSECTS;
  }
}
