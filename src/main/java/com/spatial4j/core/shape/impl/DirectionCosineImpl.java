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

import com.spatial4j.core.shape.DirectionCosinePoint;

/**
 * Implementation of a direction cosine point which represents a point on the surface
 * of the spheroidal model of the earth
 */
public class DirectionCosineImpl implements DirectionCosinePoint {

    /**
     * Data - alpha, beta, and gamma coordinates
     */
    private double alpha;
    private double beta;
    private double gamma;

    /**
     * Private Constructor - Create an Empty DirectionCosine
     * point (should never create a point without data)
     */
    private DirectionCosineImpl() {
        this.alpha = 0;
        this.beta = 0;
        this.gamma = 0;
    }

    /**
     * Constructor: Create a direction cosine point from a, b, g
     */
    public DirectionCosineImpl( double a, double b, double g ) {
        this.alpha = a;
        this.beta = b;
        this.gamma = g;
    }

    /**
     * Reset the state of the direction cosine point given arguments. This feature
     * is a performance optimization to avoid excessive shape object allocation and
     * argument control.
     */
    public void reset( double a, double b, double g ) {
        this.alpha = a;
        this.beta = b;
        this.gamma = g;
    }

    /**
     * Set the X coordinate in the 3D geocentric point
     */
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Get the Y coordinate in the 3D geocentric point
     */
    public double getBeta() {
        return this.beta;
    }

    /**
     * Get the Z coordinate in the 3D geocentric point
     */
    public double getGamma() {
        return this.gamma;
    }


    /**
     * Get Center: Return The Direciton cosine point itself
     * Provides some shape like behavior of teh 3D geocentric point
     */
    public DirectionCosineImpl getCenter() {
        return this;
    }

    /**
     * Determine if the shape has any area (internal)
     */
    public boolean hasArea() {
        return false;
    }

    /**
     * Provide a string representation of the Direction Cosine point
     */
    @Override
    public String toString() {
        return "Pt(alpha="+alpha+",beta="+beta+",gamma="+gamma+")";
    }

    @Override
    public boolean equals(Object o) {
        return equals(this, o);
    }

    /**
     *  All Direction Cosine Point implementations should use this definition of Object#equals(Object)
     */
    public static boolean equals(DirectionCosinePoint thiz, Object o) {
        assert thiz != null;
        if (thiz == o) return true;
        if (!(o instanceof DirectionCosinePoint)) return false;

        DirectionCosinePoint point = (DirectionCosinePoint) o;

        if (Double.compare(point.getAlpha(), thiz.getAlpha()) != 0) return false;
        if (Double.compare(point.getBeta(), thiz.getBeta()) != 0) return false;
        if (Double.compare(point.getGamma(), thiz.getGamma()) != 0) return false;

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
    public static int hashCode(DirectionCosinePoint thiz )   {
        int result;
        long temp;
        temp = thiz.getAlpha() != +0.0d ? Double.doubleToLongBits(thiz.getAlpha()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = thiz.getBeta() != +0.0d ? Double.doubleToLongBits(thiz.getBeta()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
