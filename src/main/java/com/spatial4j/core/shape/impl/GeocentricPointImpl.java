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

import com.spatial4j.core.shape.GeocentricPoint;
import com.spatial4j.core.shape.Vector3D;

/**
 * @file: GeocentricPointImpl.java
 * @brief: Implementation of a Geocentric Point
 * @author: Rebecca Alford (ralford)
 *
 * @details Implementation of a 3D Geocentric point which defines a point
 * on the surface of a sphere in XYZ Euclidean space.
 *
 * @note Last Modified: 2/8/14
 */
public class GeocentricPointImpl extends Vector3D implements GeocentricPoint {

    /**
     * Store Geocentric Point Components
     */
    private double x;
    private double y;
    private double z;

    /**
     * @brief Private Constructor - Create an Empty Geocentric point
     * (should never create a point without data)
     */
    private GeocentricPointImpl() {}

    /**
     * @brief Standard Constructor: Create a geocentric point from x, y, and z
     */
    public GeocentricPointImpl( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @brief Reset the 3D Geodetic Point
     */
    @Override
    public void reset( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @brief Get the X coordinate of the 3D point
     */
    @Override
    public double getX() {
        return this.x;
    }

    /**
     * @brief Get the Y coordinate of the 3D point
     */
    @Override
    public double getY() {
        return this.y;
    }

    /**
     * @brief Get the Z coordinate of the 3D point
     */
    @Override
    public double getZ() {
        return this.z;
    }

    /**
     * @brief Provide a string representation of the 3D geocentric point
     */
    @Override
    public String toString() {
        return "Pt(x="+x+",y="+y+",z="+z+")";
    }

    @Override
    public boolean equals(Object o) {
        return equals( this, o);
    }

    /**
     *  @brief Equality between Geocentric Points
     *  @details All Geocentric Point implementations should use this definition of Object#equals(Object)
     */
    public static boolean equals(GeocentricPointImpl thiz, Object o) {
        assert thiz != null;
        if (thiz == o) return true;
        if (!(o instanceof GeocentricPoint)) return false;

        GeocentricPointImpl point = (GeocentricPointImpl) o;

        if (Double.compare(point.getX(), thiz.getX()) != 0) return false;
        if (Double.compare(point.getY(), thiz.getY()) != 0) return false;
        if (Double.compare(point.getZ(), thiz.getZ()) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * @brief HashCode for Geocentric Point
     * @details All GeocentricPoint implementations should use this definition of Object#hashCode()
     * Using the same hashCode definition as teh XY point
     */
    public static int hashCode(GeocentricPointImpl thiz )   {
        int result;
        long temp;
        temp = thiz.getX() != +0.0d ? Double.doubleToLongBits(thiz.getX()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = thiz.getY() != +0.0d ? Double.doubleToLongBits(thiz.getY()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
