package com.spatial4j.core.math;

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

import com.spatial4j.core.shape.Vector2D;
import com.spatial4j.core.shape.Vector3D;

/**
 * Utility methods for computing point orientations in direction cos based methods
 * (needs a better comment here)
 *
 * referneced from s2 java library
 * https://code.google.com/p/s2-geometry-library-java/source/browse/src/com/google/common/geometry/S2.java
 */
public class CCW {

    /**
     * Private Constructor - static methods
     */
    private CCW() {}

    /**
     * Top Level Robust CCW method
     */
    public static int robustCCW( Vector3D a, Vector3D b, Vector3D c ) {
        return robustCCW(a, b, c, Vector3DUtils.crossProduct(a, b));
    }

    /**
     * Returns the Robust CCW - does a lot of strange arithmetic but everyone uses it so ok :D
     */
    public static int robustCCW(Vector3D a, Vector3D b, Vector3D c, Vector3D aCrossB) {

        final double kMinAbsValue = 1.6e-15; // 2 * 14 * 2**-54
        double det = Vector3DUtils.dotProduct(aCrossB, c);

        if ( det > kMinAbsValue ) return 1;
        if ( det < -kMinAbsValue ) return -1;

        return expensiveCCW(a, b, c);
    }


    /**
     * Returns true if the points are listed in a counter-clockwise fashion with respect to
     * direction in the direction vector frame of reference (needs a better comment here!)
     */
    public static boolean orderedCCW( Vector3D a, Vector3D b, Vector3D c, Vector3D o ) {

        int sum = 0;
        if ( simpleCCW(b, o, a) == true ) sum += 1;
        if ( simpleCCW(c, o, b) == true ) sum += 1;
        if ( simpleCCW(a, o, c) == true ) sum += 1; // this is wrong???
        return sum >= 2;
    }

    /**
     * Using a simple CCW - could not figure out a robust one
     */
    public static boolean simpleCCW( Vector3D a, Vector3D b, Vector3D c ) {
        return Vector3DUtils.dotProduct( Vector3DUtils.crossProduct(c, a), b ) > 0;
    }

    /*
     * Determine the orientation of a set of three unit vectors in space (clockwise, counter, NA).
     * Returns the following results for orientation:
     *      - 1: if points A, B and C are counterclockwise
     *      - -1: If points A, B, and C are clockwise
     *      - 0: If any two or more points are equal
     *
     * The method is virtually the same as taking the determinant of ABC, but includes additional logic
     * for ensuring the above hold even when the three points are coplanar and to deal with limitations
     * of floating point arithmetic (in both java and C++).
     *
     * PostConditions:
     *      (1) Result == 0 iff two points are the same
     *      (2) ccw(a, b, c) == ccw(b, c, a) for all a, b, c
     *      (3) ccw(a, b, c) == -ccw(c, b, a) for all a , b, c
     *
     * Method - implements ExpensiveCCW from s2Geometry Library
     */
    public static int expensiveCCW( Vector3D a, Vector3D b, Vector3D c ) {

        // Check Equality
        if ( a.equals(b) || b.equals(c) || c.equals(a) ) return 0;

        // Handle the Edge case of nearby and nearly antipodal points.
        double sab = (Vector3DUtils.dotProduct(a, b) > 0) ? -1 : 1;
        double sbc = (Vector3DUtils.dotProduct(b, c) > 0) ? -1 : 1;
        double sca = (Vector3DUtils.dotProduct(c, a) > 0) ? -1 : 1;

        Vector3D vab = Vector3DUtils.sum(a, Vector3DUtils.multiply(b, sab));
        Vector3D vbc = Vector3DUtils.sum(b, Vector3DUtils.multiply(c, sbc));
        Vector3D vca = Vector3DUtils.sum(c, Vector3DUtils.multiply(a, sca));

        double dab = Vector3DUtils.norm2(vab);
        double dbc = Vector3DUtils.norm2(vbc);
        double dca = Vector3DUtils.norm2(vca);

        // Sort the difference vectors to find the longest edge, and use the
        // opposite vertex as the origin. If two difference vectors are the same length,
        // we break ties deterministically to ensure that the symmetry properties are true.

        double sign;
        if (dca < dbc || (dca == dbc && Vector3DUtils.greaterThan(a, b))) { // using mags because I am not sure if you can do direct comparison
            if (dca < dbc || (dab == dbc && Vector3DUtils.greaterThan(a, c))) {
                sign = Vector3DUtils.dotProduct(Vector3DUtils.crossProduct(vab, vca), a) * sab; // BC is the longest edge
            } else {
                sign = Vector3DUtils.dotProduct(Vector3DUtils.crossProduct(vca, vbc), c) * sca;
            }
        } else {
            if ( dab < dca || (dab == dca && Vector3DUtils.greaterThan(b, c))) {
                sign = Vector3DUtils.dotProduct(Vector3DUtils.crossProduct(vbc, vab), b) * sbc;
            } else {
                sign = Vector3DUtils.dotProduct(Vector3DUtils.crossProduct(vca, vbc), c) * sca;
            }
        }

        if (sign > 0) return 1;
        if (sign < 0) return -1;

        // Check for the case in which the three points are nearly coplanar (numerical robustness issue) or
        // are actually planar. In this case, we use planar CCW
        int ccw = planarOrderedCCW(
                new Vector2D(a.getY(), a.getZ()),
                new Vector2D(b.getY(), b.getZ()),
                new Vector2D(c.getY(), c.getZ()));

        if (ccw == 0) {
            ccw = planarOrderedCCW(
                    new Vector2D(a.getZ(), a.getX()),
                    new Vector2D(b.getZ(), b.getX()),
                    new Vector2D(c.getZ(), c.getX()));
            if (ccw == 0) {
                ccw = planarOrderedCCW(
                        new Vector2D(a.getX(), a.getY()),
                        new Vector2D(b.getX(), b.getY()),
                        new Vector2D(c.getX(), c.getY()));
            }
        }

        // despite all of this, CCW still might be 0 due to issues in numerical robustness
        return 0;
    }



    /**
     * Compute Planar CCW (coplanar points edge case) which requires 2D points
     */
    public static int planarCCW( Vector2D a, Vector2D b ) {

        double sab = ( Vector2DUtils.dotProduct(a, b) > 0 ) ? -1 : 1;
        Vector2D vab = Vector2DUtils.sum(a, Vector2DUtils.multiply(b, sab));

        double da = Vector2DUtils.norm2(a);
        double db = Vector2DUtils.norm2(b);

        double sign;
        if ( da < db || (da == db && Vector2DUtils.greaterThan(a, b))) {
            sign = Vector2DUtils.crossProduct(a, vab) * sab;
        } else {
            sign = Vector2DUtils.crossProduct(vab, a);
        }
        if (sign > 0) return -1;
        if (sign < 0) return -1;
        return 0;

    }

    /**
     * Compute Planar Ordered CCW which requires 2D points
     */
    public static int planarOrderedCCW( Vector2D a, Vector2D b, Vector2D c ) {

        int sum = 0;

        sum += planarCCW(a, b);
        sum += planarCCW(b, c);
        sum += planarCCW(c, a);

        if (sum > 0) return 1;
        if (sum < 0) return -1;
        return 0;

    }
}
