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

/**
 * Implementation of a 3D Geocentric point which defines a point
 * on the surface of a sphere in XYZ Euclidean space.
 */
public class GeocentricPointImpl implements GeocentricPoint {

    /**
     * Data - X, Y, and Z coordinates
     */
    private double x;
    private double y;
    private double z;

    /**
     * Private Constructor - Create an Empty Geocentric
     * point (should never create a point without data)
     */
    private GeocentricPointImpl() {}

    /**
     * Constructor: Create a geocentric point from x, y, and z
     */
    public GeocentricPointImpl( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Reset the 3D point
     */
    @Override
    public void reset( double x, double y, double z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the X coordinate of the 3D point
     */
    public double getX() {
        return this.x;
    }

    /**
     * Get the Y coordinate of the 3D point
     */
    public double getY() {
        return this.y;
    }

    /**
     * Get the Z coordinate of the 3D point
     */
    public double getZ() {
        return this.z;
    }

    /**
     * Get Center: Return The Geocentric point itself
     * Provides some shape like behavior of teh 3D geocentric point
     */
    public GeocentricPoint getCenter() {
        return this;
    }

    /**
     * Determine if the shape has any area (internal)
     */
    public boolean hasArea() {
        return false;
    }

    /**
     * Provide a string representation of the 3D geocentric point
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
     *  All Geocentric Point implementations should use this definition of Object#equals(Object)
     */
    public static boolean equals(GeocentricPoint thiz, Object o) {
        assert thiz != null;
        if (thiz == o) return true;
        if (!(o instanceof GeocentricPoint)) return false;

        GeocentricPoint point = (GeocentricPoint) o;

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
     * All GeocentricPoint implementations should use this definition of Object#hashCode()
     * Using the same hashCode definition as teh XY point
     */
    public static int hashCode(GeocentricPoint thiz )   {
        int result;
        long temp;
        temp = thiz.getX() != +0.0d ? Double.doubleToLongBits(thiz.getX()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = thiz.getY() != +0.0d ? Double.doubleToLongBits(thiz.getY()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
