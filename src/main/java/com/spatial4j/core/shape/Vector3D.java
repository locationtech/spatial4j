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
 * @file: Vector3D.java
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
     * Determine if two Vector3Ds are equal
     */
    @Override
    public boolean equals(Object other) {
        return equals(this, other);
    }

    /**
     * All implementations of Vector3D should use this .equals definition
     */
    public boolean equals( Vector3D thiz, Object o ) {
        assert thiz != null;
        if (thiz == o) return true;
        if (!(o instanceof Vector3D)) return false;

        Vector3D v = (Vector3D) o;

        if ( thiz.getX() != v.getX() ) return false;
        if ( thiz.getY() != v.getY() ) return false;
        if ( thiz.getZ() != v.getZ() ) return false;

        return true;
    }
}
