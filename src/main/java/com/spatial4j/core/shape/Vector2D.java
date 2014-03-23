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
    private final double X;
    private final double Y;

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
    public boolean equals( Vector2D thiz, Object o ) {
        assert thiz != null;
        if (thiz == o) return true;
        if (!(o instanceof Vector2D)) return false;

        Vector2D v = (Vector2D) o;

        if ( thiz.getX() != v.getX() ) return false;
        if ( thiz.getY() != v.getY() ) return false;

        return true;
    }
}
