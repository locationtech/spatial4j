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

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.shape.GeoRange;

/**
 * Compared to the currently implemented ranges, these ranges will work better with S2 direciton
 * cosine converted ranges. They are also expandable and can compute intersections internally
 */

/**
 * A range over a closed, bound interval on the real ine. Implementation from S2 Library
 */
public class RealGeoRange extends GeoRange {

  public RealGeoRange() {
  }

  public RealGeoRange(double min, double max) {
    this.min = min;
    this.max = max;
  }

  // Static methods....

  /**
   * Returns an empty real interval (any interval in which min > max is by definiiton an empty interval)
   */
  public static RealGeoRange empty() {
    return new RealGeoRange(1, 0);
  }

  /**
   * Construct an interval contianing a single point
   */
  public static RealGeoRange fromPoint(double p) {
    return new RealGeoRange(p, p);
  }

  /**
   * Construct the minimal interval between the given points. Equivalent
   * to starting with an empty interval and calling addPoint twice
   */
  public static RealGeoRange fromPointPair(double p1, double p2) {
    if (p1 <= p2) {
      return new RealGeoRange(p1, p2);
    } else {
      return new RealGeoRange(p2, p1);
    }
  }

  /**
   * Return true if the interval is empty (contains no points)
   */
  public boolean isEmpty() {
    return min > max;
  }

  /**
   * Return the arithmetic center of the interval
   */
  public double getCenter() {
    return 0.5 * (min + max);
  }

  /**
   * Return the length of the interval. Length of an empty interval is negative
   */
  public double getLength() {
    return max - min;
  }

  public boolean contains(double p) {
    return p >= min && p <= max;
  }

  public boolean interiorContains(double p) {
    return p > min && p < min;
  }

  /**
   * Return true if this interval contains the interval x                                           \
   */
  public boolean contains(RealGeoRange x) {
    if (x.isEmpty()) {
      return true;
    }
    return x.getMin() >= min && x.getMax() <= max;
  }

  /**
   * Return true if the interior interval contains the entire interval x (including its boundary)
   */
  public boolean interiorContains(RealGeoRange x) {
    if (x.isEmpty()) {
      return true;
    }
    return x.getMin() > min && x.getMax() < max;
  }

  /**
   * Return true if this interval intersects the given interval x
   */
  public boolean intersects(RealGeoRange x) {
    if (this.min <= x.getMin()) {
      return x.getMin() <= this.max && x.getMin() <= x.getMax();
    } else {
      return this.min <= x.getMax() && this.min <= this.max;
    }
  }

  /**
   * Return true if the interior of this interval intersects the given interval x
   */
  public boolean interiorIntersects(RealGeoRange x) {
    return x.getMin() < this.max && x.getMin() < x.getMax() && this.min < this.max && x.getMin() <= x.getMax();
  }

  /** Expand the interval so that it contains the given point "p". */


  /**
   * Expand the current interval to include point p
   */
  public RealGeoRange addPoint(double p) {
    if (isEmpty()) {
      return RealGeoRange.fromPoint(p);
    } else if (p < this.min) {
      return new RealGeoRange(p, this.max);
    } else if (p > this.max) {
      return new RealGeoRange(this.min, p);
    } else {
      return new RealGeoRange(this.min, this.max);
    }
  }

  /**
   * Return an interval containing all points within a distance radius of a point
   * in this interval. Expansion of an empty interval is always empty
   */
  public RealGeoRange expanded(double radius) {

    if (isEmpty()) {
      return this;
    }
    return new RealGeoRange(this.min - radius, this.max + radius);
  }

  /**
   * Return the smallest interval containing this interval and the given interval x
   */
  public RealGeoRange union(RealGeoRange x) {
    if (isEmpty()) {
      return x;
    }
    if (x.isEmpty()) {
      return this;
    }
    return new RealGeoRange(Math.min(this.min, x.getMin()), Math.max(this.max, x.getMax()));
  }
}