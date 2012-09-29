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
 * A circle, also known as a point-radius since that is what it is comprised of.
 */
public abstract class Circle implements Shape {

  /**
   * Expert: Resets the state of this shape given the arguments. This is a
   * performance feature to avoid excessive Shape object allocation as well as
   * some argument error checking. Mutable shapes is error-prone so use with
   * care.
   */
  public abstract void reset(double x, double y, double radiusDEG);

  /**
   * The distance from the point's center to its edge, measured in the same
   * units as x & y (e.g. degrees if WGS84).
   */
  public abstract double getRadius();


  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Circle)) {
      return false;
    }

    Circle circle = (Circle) obj;

    return this.getCenter().equals(circle.getCenter()) &&
        Double.compare(circle.getRadius(), this.getRadius()) == 0;
  }

  @Override
  public final int hashCode() {
    int result = this.getCenter().hashCode();
    long temp = this.getRadius() != +0.0d ? Double.doubleToLongBits(this.getRadius()) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Circle(" + getCenter() + ", d=" + getRadius() + "°)";
  }
}
