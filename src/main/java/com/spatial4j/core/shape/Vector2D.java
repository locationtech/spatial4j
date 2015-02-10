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
 * Generic 2 Component Vector
 */
public class Vector2D {

  /**
   * Store 2 doubles as vector components
   */
  private double X;
  private double Y;

  /**
   * Constructors
   */

  /**
   * Default constructor for a 2D Vector - Initializes all components to 0.
   */
  public Vector2D() {
    this.X = 0;
    this.Y = 0;
  }

  /**
   * Public constructor for a 3 component vector (generic)
   */
  public Vector2D(double x, double y) {
    this.X = x;
    this.Y = y;
  }

  /**
   * Accessor methods
   */

  /**
   * Get the x component of the 2D point
   */
  public double getX() {
    return this.X;
  }

  /**
   * Get the y component of the 2D point
   */
  public double getY() {
    return this.Y;
  }

  /**
   * Determine if two Vector2Ds are equal
   */
  @Override
  public boolean equals(Object other) {
    return equals(this, other);
  }

  /**
   * All implementations of Vector3D should use this .equals definition
   */
  public boolean equals(Vector2D thiz, Object o) {
    assert thiz != null;
    if (thiz == o) return true;
    if (!(o instanceof Vector2D)) return false;

    Vector2D v = (Vector2D) o;

    if (thiz.getX() != v.getX()) return false;
    if (thiz.getY() != v.getY()) return false;

    return true;
  }

  /**
   * Compute the sum of two vectors
   */
  public static Vector2D add(Vector2D a, Vector2D b) {

    double sum_x = a.getX() + b.getX();
    double sum_y = a.getY() + b.getY();

    return new Vector2D(sum_x, sum_y);
  }

  /**
   * Compute the difference between two vectors
   */
  public static Vector2D subtract(Vector2D a, Vector2D b) {

    double diff_x = a.getX() - b.getX();
    double diff_y = a.getY() - b.getY();

    return new Vector2D(diff_x, diff_y);
  }


  /**
   * Compute the product of a vector and a scalar factor s.
   */
  public static Vector2D multiply(Vector2D v, double s) {

    double mult_x = s * v.getX();
    double mult_y = s * v.getY();

    return new Vector2D(mult_x, mult_y);
  }

  /**
   * Compute the scalar magnitude of a vector
   */
  public double length() {

    double x_2 = Math.pow(this.X, 2);
    double y_2 = Math.pow(this.Y, 2);

    return Math.sqrt(x_2 + y_2);
  }

  /**
   * Compute the unit vector of the given vector
   */
  public Vector2D unitVector() {
    return multiply(this, 1 / length());
  }

  /**
   * Compute the cross product between two 2D vectors (apparently this is mathematically valid??)
   * returns a scalar in 2D
   */
  public double crossProduct(Vector2D v) {
    return getX() * v.getY() - getY() * v.getX();
  }

  /**
   * Compute the dot product between two vectors
   */
  public double dotProduct(Vector2D v) {

    double x = getX() * v.getX();
    double y = getY() * v.getY();

    return x + y;
  }


  /**
   * Standard Normalize
   */
  public static Vector2D normalize(Vector2D a) {
    double n = norm(a);
    if (n != 0) n = 1.0 / n;
    return multiply(a, n);
  }

  /**
   * Norm2
   */
  public static double norm2(Vector2D a) {
    return Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2);
  }

  /**
   * Norm
   */
  public static double norm(Vector2D a) {
    return Math.sqrt(norm2(a));
  }

  /**
   * Vector Comparison: B greater than A
   */
  public static boolean greaterThan(Vector2D a, Vector2D b) {
    if (a.getX() < b.getX()) return true;
    if (b.getX() < a.getX()) return false;
    if (a.getY() < b.getY()) return true;
    return false;

  }
}
