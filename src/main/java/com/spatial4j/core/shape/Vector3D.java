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
 * @file: Vector3D.java  (should update doucmentation and sort statics and noon statics)
 * @brief: Generic 3 Component Vector/Point (double)
 * @author: Rebecca Alford (ralford)
 *
 * @details This class provides a generic abstraction of a 3 component vector from the origin (0, 0, 0). This
 * class is intended to be lightweight and allow easily abstraction for reuse of vector utilities. This class
 * might be used alone as a 3D vector and also as the base class for DirectionCosine Point and GeocentricPoint
 * which are both representations of a point on the spheroidal model of the earth.
 *
 * @note Last Modified: 2/8/14
 */
public class Vector3D {

    /**
     * Store 3 doubles as vector components
     */
    private final double X;
    private final double Y;
    private final double Z;

    /**
     * Constructors
     */

    /**
     * @brief Default constructor for a 3D Vector - Initializes all components to 0.
     */
    public Vector3D() {
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
    }

    /**
     * @brief Public constructor for a 3 component vector (generic)
     */
    public Vector3D(double x, double y, double z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    /**
     * Accessor methods
     */

    /**
     * @brief Get the x component of the 3D point
     */
    public double getX() {
        return this.X;
    }

    /**
     * @brief Get the y component of the 3D point
     */
    public double getY() {
        return this.Y;
    }

    /**
     * @brief Get the z component of the 3D point
     */
    public double getZ() {
        return this.Z;
    }


    /**
     * Computations with Vectors
     */

    // Subtract 2 vectors
    public static Vector3D minus( Vector3D v1, Vector3D v2 ) {
        return sub(v1, v2);
    }

    // Negate a vector
    public static Vector3D neg( Vector3D v ) {
        return new Vector3D( -v.X, -v.Y, -v.Z );
    }

    // Normalize Vector squared
    public double norm2() {
        return Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2);
    }

    // Compute norm
    public double norm() {
        return Math.sqrt( norm2() );
    }

    // Inside compute cross product
    public Vector3D crossProd( final Vector3D v ) {
        return Vector3D.crossProd(this, v);
    }

    // Outside Compute Cross Product between two vectors
    public static Vector3D crossProd( final Vector3D v1, final Vector3D v2 ) {
        return new Vector3D(
                v1.Y * v2.Z - v1.Z * v2.Y,
                v1.Z * v2.X - v1.X * v2.Z,
                v1.X * v2.Y - v1.Y * v2.X);
    }

    // Add 2 vectors
    public static Vector3D add( final Vector3D v1, final Vector3D v2 ) {
        return new Vector3D( v1.X + v2.X, v1.Y + v2.Y, v1.Z + v2.Z );
    }


    // Subtract 2 Vectors (internal final subtraction)
    public static Vector3D sub(final Vector3D v1, final Vector3D v2) {
        return new Vector3D(v1.X- v2.X, v1.Y - v2.Y, v1.Z - v2.Z);
    }

    // Compute the dot product between this Vector3D and another point
    public double dotProduct( Vector3D v ) {
        return this.X * v.X + this.Y * v.Y + this.Z * v.Z;
    }

    // Multiply some vector by a scalar
    public static Vector3D multiply( final Vector3D v, double m ) {
        return new Vector3D( m*v.X, m*v.Y, m*v.Z );
    }

    // Divide some vector by a scalar
    public static Vector3D divide( final Vector3D v, double m ) {
        return new Vector3D( v.X/m, v.Y/m, v.Z/m );
    }

    // Return a Vector Orthogonal to this one
    public Vector3D ortho() {
        int k = largestAbsComponent();
        Vector3D temp;

        if (k == 1) temp = new Vector3D(1, 0, 0);
        else if (k == 2) temp = new Vector3D(0, 1, 0);
        else temp = new Vector3D(0, 0, 1);

        return Vector3D.normalize( crossProd(this, temp) );
    }

    // Return the index of the largest abs component
    public int largestAbsComponent() {
        Vector3D temp = fabs(this);
        if (temp.X > temp.Y) {
            if (temp.X > temp.Z) {
                return 0;
            } else {
                return 2;
            }
        } else {
            if (temp.Y > temp.Z) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    public static Vector3D fabs(Vector3D v) {
        return new Vector3D(Math.abs(v.X), Math.abs(v.Y), Math.abs(v.Z));
    }

    public static Vector3D normalize(Vector3D v) {
        double norm = v.norm();
        if (norm != 0) {
            norm = 1.0 / norm;
        }
        return Vector3D.multiply(v, norm);
    }

    public double get(int axis) {
        return (axis == 0) ? X : (axis == 1) ? Y : Z;
    }

    /** Return the angle between two vectors in radians */
    public double angle(Vector3D v) {
        return Math.atan2( crossProd(this, v).norm(), this.dotProduct(v) );
    }

}
// can still implement and test more, but working on it