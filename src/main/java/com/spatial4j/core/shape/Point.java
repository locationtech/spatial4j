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
 * A Point with X & Y coordinates.
 */
public abstract class Point implements Shape {

  /**
   * Expert: Resets the state of this shape given the arguments. This is a
   * performance feature to avoid excessive Shape object allocation as well as
   * some argument error checking. Mutable shapes is error-prone so use with
   * care.
   */
  public abstract void reset(double x, double y);

  /** The X coordinate, or Longitude in geospatial contexts. */
  public abstract double getX();

  /** The Y coordinate, or Latitude in geospatial contexts. */
  public abstract double getY();

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Point)) {
      return false;
    }

    Point point = (Point) o;

    return Double.compare(point.getX(), this.getX()) == 0 &&
        Double.compare(point.getY(), this.getY()) == 0;
  }

  @Override
  public final int hashCode() {
    long temp = this.getX() != +0.0d ? Double.doubleToLongBits(this.getX()) : 0L;
    int result = (int) (temp ^ (temp >>> 32));
    temp = this.getY() != +0.0d ? Double.doubleToLongBits(this.getY()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Pt(x=" + getX() + ",y=" + getY() + ")";
  }
}
