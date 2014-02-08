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
 * Utilities for operations on 3D vectors
 */
public class VectorUtils {

    private VectorUtils() {}

    /**
     * Vector Sum
     */
    public static Vector3D sum( Vector3D a, Vector3D b ) {

        double sum_x = a.getX() + b.getX();
        double sum_y = a.getY() + b.getY();
        double sum_z = a.getZ() + b.getZ();

        return new Vector3D( sum_x, sum_y, sum_z );
    }

    /**
     * Vector Difference
     */
    public static Vector3D difference( Vector3D a, Vector3D b ) {

        double diff_x = a.getX() - b.getX();
        double diff_y = a.getY() - b.getY();
        double diff_z = a.getZ() - b.getZ();

        return new Vector3D( diff_x, diff_y, diff_z );
    }


    /**
     * Scalar multiplication
     */
    public static Vector3D multiply( Vector3D v, double s ) {

        double mult_x = s * v.getX();
        double mult_y = s * v.getY();
        double mult_z = s * v.getZ();

        return new Vector3D( mult_x, mult_y, mult_z );
    }

    /**
     * Vector Magnitude
     */
    public static double mag( Vector3D v  ) {

        double x_2 = Math.pow( v.getX(), 2 );
        double y_2 = Math.pow( v.getY(), 2 );
        double z_2 = Math.pow( v.getZ(), 2 );

        return Math.sqrt( x_2 + y_2 + z_2 );
    }

    /**
     * Compute the unit vector of the given 3D Vector
     * Specifies direction and guarantees a magnitude == 1
     */
    public static Vector3D unit_vector( Vector3D v ) {
        double mag = mag(v);
        return multiply(v, 1/mag);
    }
}
