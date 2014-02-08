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
 * @details This cass represents a generalized 3D point. The purpose of this class is to provide a lightweight
 * representation of a 3 double component object and is currently the base cass for a GeocentricPoint and
 * DirectionCosine Point. This vector will enable these points to be used in vector utilities.
 *
 * @note Last Modified: 2/8/14
 */
public class Vector3D {

    /**
     * Store coordinate information
     */
    private final double X;
    private final double Y;
    private final double Z;

    /**
     * Constructors
     */

    /**
     * Default constructor for a 3D Point - Initialzies all components
     * to 0.
     */
    public Vector3D() {
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
    }

    /**
     * Public constructor for a 3 component point (generic)
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
     * Get the x component of the 3D point
     */
    public double getX() {
        return this.X;
    }

    /**
     * Get the y component of the 3D point
     */
    public double getY() {
        return this.Y;
    }

    /**
     * Get the z component of the 3D point
     */
    public double getZ() {
        return this.Z;
    }
}
