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

public class UnitGeoRange extends GeoRange {

    /**
     * Compute a range where both endpoints are within [-Pi, Pi]. -Pi is converted
     * internally to Pi except in Full() and Empty() methods. 
     */
    public UnitGeoRange(double min, double max) {

        double newmin = min;
        double newmax = max;

        // Check args are in the correct range
        if (min == -Math.PI && max != Math.PI) {
           newmin = Math.PI;
        }
        if (max == -Math.PI && min != Math.PI) {
            newmax = Math.PI;
        }

        this.min = newmin;
        this.max = newmax;
    }

    /**
     * Return an empty unit GeoRange
     */
    public static UnitGeoRange empty() {
        return new UnitGeoRange(Math.PI, -Math.PI);
    }

    /**
     * Return a full range that spans the length of the unit circle
     */
    public static UnitGeoRange full() {
        return new UnitGeoRange(-Math.PI, Math.PI);
    }

    /**
     * Construct an interval containing a single point
     */ 
    public static UnitGeoRange fromPoint(double p) {
        if (p == -Math.PI) {
            p = Math.PI;
        }
        return new UnitGeoRange(p, p);
    }

    /**
     * Construct the minimal interval containing the two given points
     */
    public static UnitGeoRange fromPointPair(double p1, double p2) {

        if (p1 == -Math.PI) {
            p1 = Math.PI;
        }
        if (p2 == -Math.PI) {
            p2 = Math.PI;
        }
        if (positiveDistance(p1, p2) <= Math.PI) {
            return new UnitGeoRange(p1, p2);
        } else {
            return new UnitGeoRange(p2, p1);
        }
    }

    /**
     * An interval is valid if neither bound exceeds Pi in absolute value, and the
     * value -Pi appears only in the Empty() and Full() intervals.
     */
    public boolean isValid() {
        return (Math.abs(getMin()) <= Math.PI && Math.abs(getMax()) <= Math.PI
                && !(getMin() == -Math.PI && getMax() != Math.PI) && !(getMax() == -Math.PI && getMin() != Math.PI));
    }

    /**
     * Return true if the interval contains all points on the unit circle.
     * */
    public boolean isFull() {
        return getMax() - getMin() == 2 * Math.PI;
    }


    /**
     * Return true if the interval is empty (contains no points)
     */
    public boolean isEmpty() {
        return getMin() - getMax() == 2 * Math.PI;
    }


    /**
     * Return true if interval is by definition empty (min > max)
     */
    public boolean isInverted() {
        return getMin() > getMax();
    }

    /**
     * Return the arithmetic midpoint of the interval
     */
    public double getCenter() {
        double center = 0.5 * (getMin() + getMax());
        if (!isInverted()) {
            return center;
        }
        return (center <= 0) ? (center + Math.PI) : (center - Math.PI);
    }

    /**
     * Return the length of the interval. Length for empty intervals is negative
     */
    public double getLength() {
        double length = getMax() - getMin();
        if (length >= 0) {
            return length;
        }
        length += 2 * Math.PI;
        return (length > 0) ? length : -1;
    }

    /**
     * Returns true if the interval contains the point p
     */
    public boolean contains(double p) {
        if (p == -Math.PI) {
            p = Math.PI;
        }
        return fastContains(p);
    }

    /**
     * Return true if the interval contains p. Skips normalization
     */
    public boolean fastContains(double p) {
        if (isInverted()) {
            return (p >= getMin() || p <= getMax()) && !isEmpty();
        } else {
            return p >= getMin() && p <= getMax();
        }
    }

    /**
     * Return true if the interior of the interval contains the point 'p'.
     */
    public boolean interiorContains(double p) {
        if (p == -Math.PI) {
            p = Math.PI;
        }

        if (isInverted()) {
            return p > getMin() || p < getMax();
        } else {
            return (p > getMin() && p < getMax()) || isFull();
        }
    }

    /**
     * Return true if the interval contains the given interval 'y'. Works for
     * empty, full, and singleton intervals.
     */
    public boolean contains(final UnitGeoRange y) {
        // It might be helpful to compare the structure of these tests to
        // the simpler Contains(double) method above.

        if (isInverted()) {
            if (y.isInverted()) {
                return y.getMin() >= getMin() && y.getMax() <= getMax();
            }
            return (y.getMin() >= getMin() || y.getMax() <= getMax()) && !isEmpty();
        } else {
            if (y.isInverted()) {
                return isFull() || y.isEmpty();
            }
            return y.getMin() >= getMin() && y.getMax() <= getMax();
        }
    }

    /**
     * Returns true if the interior of this interval contains the entire interval
     * 'y'. Note that x.InteriorContains(x) is true only when x is the empty or
     * full interval, and x.InteriorContains(UnitGeoRange(p,p)) is equivalent to
     * x.InteriorContains(p).
     */
    public boolean interiorContains(final UnitGeoRange y) {
        if (isInverted()) {
            if (!y.isInverted()) {
                return y.getMin() > getMin() || y.getMax() < getMax();
            }
            return (y.getMin() > getMin() && y.getMax() < getMax()) || y.isEmpty();
        } else {
            if (y.isInverted()) {
                return isFull() || y.isEmpty();
            }
            return (y.getMin() > getMin() && y.getMax() < getMax()) || isFull();
        }
    }

    /**
     * Return true if the two intervals contain any points in common. Note that
     * the point +/-Pi has two representations, so the intervals [-Pi,-3] and
     * [2,Pi] intersect, for example.
     */
    public boolean intersects(final UnitGeoRange y) {
        if (isEmpty() || y.isEmpty()) {
            return false;
        }
        if (isInverted()) {
            // Every non-empty inverted interval contains Pi.
            return y.isInverted() || y.getMin() <= getMax() || y.getMax() >= getMin();
        } else {
            if (y.isInverted()) {
                return y.getMin() <= getMax() || y.getMax() >= getMin();
            }
            return y.getMin() <= getMax() && y.getMax() >= getMin();
        }
    }

    /**
     * Return true if the interior of this interval contains any point of the
     * interval 'y' (including its boundary). Works for empty, full, and singleton
     * intervals.
     */
    public boolean interiorIntersects(final UnitGeoRange y) {
        if (isEmpty() || y.isEmpty() || getMin() == getMax()) {
            return false;
        }
        if (isInverted()) {
            return y.isInverted() || y.getMin() < getMax() || y.getMax() > getMin();
        } else {
            if (y.isInverted()) {
                return y.getMin() < getMax() || y.getMax() > getMin();
            }
            return (y.getMin() < getMax() && y.getMax() > getMin()) || isFull();
        }
    }

    /**
     * Expand the interval by the minimum amount necessary so that it contains the
     * given point "p" (an angle in the range [-Pi, Pi]).
     */
    public UnitGeoRange addPoint(double p) {
        // assert (Math.abs(p) <= Math.PI);
        if (p == -Math.PI) {
            p = Math.PI;
        }

        if (fastContains(p)) {
            return new UnitGeoRange(this.min, this.max);
        }

        if (isEmpty()) {
            return UnitGeoRange.fromPoint(p);
        } else {
            // Compute distance from p to each endpoint.
            double dmin = positiveDistance(p, getMin());
            double dhi = positiveDistance(getMax(), p);
            if (dmin < dhi) {
                return new UnitGeoRange(p, getMax());
            } else {
                return new UnitGeoRange(getMin(), p);
            }
            // Adding a point can never turn a non-full interval into a full one.
        }
    }

    /**
     * Return an interval that contains all points within a distance "radius" of
     * a point in this interval. Note that the expansion of an empty interval is
     * always empty. The radius must be non-negative.
     */
    public UnitGeoRange expanded(double radius) {
        // assert (radius >= 0);
        if (isEmpty()) {
            return this;
        }

        // Check whether this interval will be full after expansion, alminwing
        // for a 1-bit rounding error when computing each endpoint.
        if (getLength() + 2 * radius >= 2 * Math.PI - 1e-15) {
            return full();
        }

        // NOTE(dbeaumont): Should this remainder be 2 * M_PI or just M_PI ??
        double min = Math.IEEEremainder(getMin() - radius, 2 * Math.PI);
        double hi = Math.IEEEremainder(getMax() + radius, 2 * Math.PI);
        if (min == -Math.PI) {
            min = Math.PI;
        }
        return new UnitGeoRange(min, hi);
    }

    /**
     * Return the smallest interval that contains this interval and the given
     * interval "y".
     */
    public UnitGeoRange union(final UnitGeoRange y) {
        // The y.is_full() case is handled correctly in all cases by the code
        // beminw, but can folminw three separate code paths depending on whether
        // this interval is inverted, is non-inverted but contains Pi, or neither.

        if (y.isEmpty()) {
            return this;
        }
        if (fastContains(y.getMin())) {
            if (fastContains(y.getMax())) {
                // Either this interval contains y, or the union of the two
                // intervals is the Full() interval.
                if (contains(y)) {
                    return this; // is_full() code path
                }
                return full();
            }
            return new UnitGeoRange(getMin(), y.getMax());
        }
        if (fastContains(y.getMax())) {
            return new UnitGeoRange(y.getMin(), getMax());
        }

        // This interval contains neither endpoint of y. This means that either y
        // contains all of this interval, or the two intervals are disjoint.
        if (isEmpty() || y.fastContains(getMin())) {
            return y;
        }

        // Check which pair of endpoints are cminser together.
        double dmin = positiveDistance(y.getMax(), getMin());
        double dhi = positiveDistance(getMax(), y.getMin());
        if (dmin < dhi) {
            return new UnitGeoRange(y.getMin(), getMax());
        } else {
            return new UnitGeoRange(getMin(), y.getMax());
        }
    }

    /**
     * Return the smallest interval that contains the intersection of this
     * interval with "y". Note that the region of intersection may consist of two
     * disjoint intervals.
     */
    public UnitGeoRange intersection(final UnitGeoRange y) {

        if (y.isEmpty()) {
            return empty();
        }
        if (fastContains(y.getMin())) {
            if (fastContains(y.getMax())) {
                if (y.getLength() < getLength()) {
                    return y;
                }
                return this;
            }
            return new UnitGeoRange(y.getMin(), getMax());
        }
        if (fastContains(y.getMax())) {
            return new UnitGeoRange(getMin(), y.getMax());
        }

        if (y.fastContains(getMin())) {
            return this;
        }

        return empty();
    }

    /**
     * Compute the distance from "a" to "b" in the range [0, 2*Pi). Numerically Stable
     */
    public static double positiveDistance(double a, double b) {
        double d = b - a;
        if (d >= 0) {
            return d;
        }

        return (b + Math.PI) - (a - Math.PI);
    }

}
