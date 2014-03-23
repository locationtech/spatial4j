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

package com.spatial4j.core.math;

import com.spatial4j.core.shape.Vector2D;

/**
 * VectorUtils implements utilities for operations with 3 component vectors (x,y,z).
 * This class includes sum, difference, multiply, mag, unitVector, and cross product.
 *
 * Last Modified: 2/22/14
 */
public class Vector2DUtils {

    private Vector2DUtils() {}

    /**
     * Compute the sum of two vectors
     */
    public static Vector2D sum( Vector2D a, Vector2D b ) {

        double sum_x = a.getX() + b.getX();
        double sum_y = a.getY() + b.getY();

        return new Vector2D( sum_x, sum_y );
    }

    /**
     * Compute the difference between two vectors
     */
    public static Vector2D difference( Vector2D a, Vector2D b ) {

        double diff_x = a.getX() - b.getX();
        double diff_y = a.getY() - b.getY();

        return new Vector2D( diff_x, diff_y );
    }


    /**
     * Compute the product of a vector and a scalar factor s.
     */
    public static Vector2D multiply( Vector2D v, double s ) {

        double mult_x = s * v.getX();
        double mult_y = s * v.getY();

        return new Vector2D( mult_x, mult_y );
    }

    /**
     * Compute the scalar magnitude of a vector
     */
    public static double mag( Vector2D v  ) {

        double x_2 = Math.pow( v.getX(), 2 );
        double y_2 = Math.pow( v.getY(), 2 );

        return Math.sqrt( x_2 + y_2 );
    }

    /**
     * Compute the unit vector of the given vector
     */
    public static Vector2D unitVector( Vector2D v ) {
        double mag = mag(v);
        return multiply(v, 1/mag);
    }

    /**
     * Compute the cross product between two 2D vectors (apparently this is mathematically valid??)
     * returns a scalar in 2D
     */
    public static double crossProduct( Vector2D a, Vector2D b ) {
        return a.getX()*b.getY() - a.getY()*b.getX();
    }

    /**
     * Compute the dot product between two vectors
     */
    public static double dotProduct( Vector2D a, Vector2D b ) {

        double x = a.getX()*b.getX();
        double y = a.getY()*b.getY();

        return x + y;
    }


    /**
     * Standard Normalize
     */
    public static Vector2D normalize( Vector2D a ) {
        double n = norm(a);
        if ( n != 0 ) n = 1.0/n;
        return multiply(a, n);
    }

    /**
     * Norm2
     */
    public static double norm2( Vector2D a ) {
        return Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2);
    }

    /**
     * Norm
     */
    public static double norm( Vector2D a ) {
        return Math.sqrt(norm2(a));
    }

    /**
     * Vector Comparison: B greater than A
     */
    public static boolean greaterThan( Vector2D a, Vector2D b ) {
        if ( a.getX() < b.getX() ) return true;
        if ( b.getX() < a.getX() ) return false;
        if ( a.getY() < b.getY() ) return true;
        return false;

    }
}
