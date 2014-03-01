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

import com.spatial4j.core.shape.Vector3D;

/**
 * VectorUtils implements utilities for operations with 3 component vectors (x,y,z).
 * This class includes sum, difference, multiply, mag, unitVector, and cross product.
 *
 * Last Modified: 2/22/14
 */
public class VectorUtils {

    private VectorUtils() {}

    /**
     * Compute the sum of two vectors
     */
    public static Vector3D sum( Vector3D a, Vector3D b ) {

        double sum_x = a.getX() + b.getX();
        double sum_y = a.getY() + b.getY();
        double sum_z = a.getZ() + b.getZ();

        return new Vector3D( sum_x, sum_y, sum_z );
    }

    /**
     * Compute the difference between two vectors
     */
    public static Vector3D difference( Vector3D a, Vector3D b ) {

        double diff_x = a.getX() - b.getX();
        double diff_y = a.getY() - b.getY();
        double diff_z = a.getZ() - b.getZ();

        return new Vector3D( diff_x, diff_y, diff_z );
    }


    /**
     * Compute the product of a vector and a scalar factor s.
     */
    public static Vector3D multiply( Vector3D v, double s ) {

        double mult_x = s * v.getX();
        double mult_y = s * v.getY();
        double mult_z = s * v.getZ();

        return new Vector3D( mult_x, mult_y, mult_z );
    }

    /**
     * Compute the scalar magnitude of a vector
     */
    public static double mag( Vector3D v  ) {

        double x_2 = Math.pow( v.getX(), 2 );
        double y_2 = Math.pow( v.getY(), 2 );
        double z_2 = Math.pow( v.getZ(), 2 );

        return Math.sqrt( x_2 + y_2 + z_2 );
    }

    /**
     * Compute the unit vector of the given vector
     */
    public static Vector3D unitVector( Vector3D v ) {
        double mag = mag(v);
        return multiply(v, 1/mag);
    }

    /**
     * Compute the cross product between two vectors
     */
    public static Vector3D crossProduct( Vector3D v1, Vector3D v2 ) {

        double x = v1.getY()*v2.getZ() - v1.getZ()*v2.getY();
        double y = v2.getZ()*v2.getX() - v1.getX()*v2.getZ();
        double z = v1.getX()*v2.getY() - v1.getY()*v2.getX();

        return new Vector3D(x, y, z);
    }

    /**
     * Compute the angle between two vectors
     */
    public static double angle( Vector3D v1, Vector3D v2 ) {
        return distance( v1, v2 );
    }

    /**
     * Compute the unit distance between the two points
     */
    public static double distance( Vector3D v1, Vector3D v2 ) {
        double x2 = Math.pow( v2.getX()-v1.getX(), 2 );
        double y2 = Math.pow( v2.getY()-v1.getY(), 2 );
        double z2 = Math.pow( v2.getZ()-v1.getZ(), 2 );

        return Math.sqrt( x2 + y2 + z2 );
    }

    /**
     * S2 Normalize
     */
    public static double norm2( Vector3D v1 ) {
        /// uhhh // TODO
    }
}
