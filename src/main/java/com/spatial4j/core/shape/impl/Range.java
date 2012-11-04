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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Rectangle;

/**
 * INTERNAL: A numeric range between a pair of numbers.
 * Perhaps this class could become 1st class citizen extending Shape but not now.
 * Only public so is accessible from tests in another package.
 */
public class Range {
  protected final double min, max;

  public static Range xRange(Rectangle rect, SpatialContext ctx) {
    if (ctx.isGeo())
      return new LongitudeRange(rect.getMinX(), rect.getMaxX());
    else
      return new Range(rect.getMinX(), rect.getMaxX());
  }

  public static Range yRange(Rectangle rect, SpatialContext ctx) {
    return new Range(rect.getMinY(), rect.getMaxY());
  }

  public Range(double min, double max) {
    this.min = min;
    this.max = max;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Range range = (Range) o;

    if (Double.compare(range.max, max) != 0) return false;
    if (Double.compare(range.min, min) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = min != +0.0d ? Double.doubleToLongBits(min) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = max != +0.0d ? Double.doubleToLongBits(max) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Range{" + min + " TO " + max + '}';
  }

  public double getWidth() {
    return max - min;
  }

  public boolean contains(double v) {
    return v >= min && v <= max;
  }

  public double getCenter() {
    return min + getWidth()/2;
  }

  public Range expandTo(Range other) {
    assert this.getClass() == other.getClass();
    return new Range(Math.min(min, other.min), Math.max(max, other.max));
  }

  public double deltaLen(Range other) {
    double min3 = Math.max(min, other.min);
    double max3 = Math.min(max, other.max);
    return max3 - min3;
  }

}

class LongitudeRange extends Range {

  public LongitudeRange(double min, double max) {
    super(min, max);
  }

  public LongitudeRange(Rectangle r) {
    super(r.getMinX(), r.getMaxX());
  }

  @Override
  public double getWidth() {
    double w = super.getWidth();
    if (w < 0)
      w += 360;
    return w;
  }

  @Override
  public boolean contains(double v) {
    if (!crossesDateline())
      return super.contains(v);
    return v >= min || v <= max;// the OR is the distinction from non-dateline cross
  }

  public boolean crossesDateline() {
    return min > max;
  }

  public double getCenter() {
    double ctr = super.getCenter();
    if (ctr > 180)
      ctr -= 360;
    return ctr;
  }

  public double compareTo(double v) {
    double ctr = getCenter();
    double diff = ctr - v;
    if (diff <= 180) {
      if (diff >= -180)
        return diff;
      return diff + 360;
    } else {
      return diff - 360;
    }
  }

  @Override
  public Range expandTo(Range other) {
    return expandTo((LongitudeRange)other);
  }

  public LongitudeRange expandTo(LongitudeRange other) {
    LongitudeRange a, b;//a is bigger
    double width1 = getWidth();
    double width2 = other.getWidth();
    if (width1 >= width2) {
      a = this;
      b = other;
    } else {
      a = other;
      b = this;
    }
    double bCtr = b.getCenter();
    double diff = a.compareTo(bCtr);
    if (diff == -180 || diff == 180) {//opposite sides of globe
      if (width1 + width2 >= 360)
        return new LongitudeRange(-180, 180);
      return new LongitudeRange(b.min, a.max);//could have chosen flip side too
    } else if (diff >= 0) {
      if (a.contains(b.min))
        return a;
      return new LongitudeRange(b.min, a.max);
    } else {
      if (a.contains(b.max))
        return a;
      return new LongitudeRange(a.min, b.max);
    }
  }
}