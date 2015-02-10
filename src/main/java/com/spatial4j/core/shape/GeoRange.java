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

/**
 * Base class for expandable geo ranges implemented in shape.impl.
 */

/**
 * Geodesic Range (Generic)
 */
public class GeoRange {

  protected double min;
  protected double max;

  public GeoRange() {
    this.min = 0;
    this.max = 0;
  }

  public GeoRange(double min, double max) {
    this.min = min;
    this.max = max;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GeoRange range = (GeoRange) o;

    if (Double.compare(range.max, max) != 0) return false;
    if (Double.compare(range.min, min) != 0) return false;

    return true;
  }

  public int hashCode() {
    int result;
    long temp;
    temp = min != +0.0d ? Double.doubleToLongBits(min) : 0L;
    result = (int) (temp ^ (temp >>> 32));
    temp = max != +0.0d ? Double.doubleToLongBits(max) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  public String toString() {
    return "GeoRange{" + min + " TO " + max + '}';
  }
}