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
 * When using direction vectors to represent the bounds of some shape, it is important to ensure that all of the direction
 * vectors (represented by Vector3Ds) hold a consistent orientation - either pointing clockwise or counter clockwise (CCW). We use
 * the counter-clockwise orientation as it is a precendent set by both the Raytheon and S2 spherical geometry projects.
 *
 * Several methods for computing CCW are included in this class. These include computing the CCCW of coplanar points,
 * ordered coplanar points, an ordered set of points. There are also both 'expensive' and 'robust' methods for computing
 * CCWs which deal with numerical robustness and precision issues that arise in computation.
 *
 * The CCW methods in this class were adapted for use with Spatial4j shapes from the following projects:
 *      S2-Java: https://code.google.com/p/s2-geometry-library-java/source/
 *      S2-C++: https://code.google.com/p/s2-geometry-library/s
 *
 * Last Updated: 3/25/14
 */
public class CCW {

    // Construct CCW
    private CCW() {}

    /**
     *  Robust Method for determining if a set of three consecutive direction vectors
     *  are of counterclockwise orientation> Returns +1 for CCW and -1 for CW. Satisfies
     *  the following invariants:
     *
     *      (1) robustCCW(a, b, c) == 0 iff a==b || b == c || c == a
     *      (2) robustCCW(b, c, a) == robustCCW(a, b, c) for all a, b, c
     *      (3) robustCCW(c, b, a) == -robustCCW(a, b, c) for all a, b, c
     *
     * **Numerically robust
     */
    public static int robustCCW( Vector3D a, Vector3D b, Vector3D c ) {
        return robustCCW(a, b, c, a.crossProd(b));
    }

    /**
     * A slightly more efficient version of robustCCW that enables the cross product of
     * a and b to be specified in advance.
     */
    public static int robustCCW(Vector3D a, Vector3D b, Vector3D c, Vector3D aCrossB) {

        final double kMinAbsValue = 1.6e-15; // 2 * 14 * 2**-54
        double det = aCrossB.dotProduct(c);

        if ( det > kMinAbsValue ) return 1;
        if ( det < -kMinAbsValue ) return -1;

        return expensiveCCW(a, b, c);
    }

    /**
     * Method for determining if a set of four points are listed in counter-clockwise orientation. Returns
     * true if edges OA, OB, and oC are encountered in that order while sweeping CCW about the point 0.
     * Equivalent to thinking if A <= B <= C with respect to a continuous CCW about 0.
     *
     * Assert the following invariants:
     *      (1) orderedCCW(a, b, c, o) && orderedCCW(b, a, c, o) then a == b
     *      (2) orderedCCW(a, b, c, o) && orderedCCW(a, c, b, o) then b == c
     *      (3) orderedCCW(a, b, c, o) && orderedCCW(c, b, a, o) then a == b == c
     *      (4) a == b or b == c then orderedCCW(a, b, c, o) == true
     *      (5) a == c then orderedCCW(a, b, c, o) == false
     *
     */
    public static boolean orderedCCW( Vector3D a, Vector3D b, Vector3D c, Vector3D o ) {

        int sum = 0;
        if ( simpleCCW(b, o, a) == true ) sum += 1;
        if ( simpleCCW(c, o, b) == true ) sum += 1;
        if ( simpleCCW(a, o, c) == true ) sum += 1; // this is wrong???
        return sum >= 2;
    }

    /**
     * Non-Robust implementation of determining if points are counter-clockwise.
     * Return true if a, b, and c are strictly CCW. Return if points are clockwise
     * or colinear (all contained in the great circle). Due to numerical errors, impercision
     * might return the wrong error.
     *
     * Assert the following invaraint:
     *  simpleCCW(a, b, c) then !simpleCCW(c, b, a) for all a, b, c
     */
    public static boolean simpleCCW( Vector3D a, Vector3D b, Vector3D c ) {
        return (a.crossProd(c)).dotProduct( b ) > 0;
    }

    /**
     * Determine the orientation of a set of three direction vectors (CCW, non-CCW, inconsistent). Returns
     * the following results:
     *      - expensiveCCW(a, b, c) == 1 if CCW
     *      - expensiveCCW(a, b, c) == -1 if CW
     *      - expensiveCCW(a, b, c) == 0 if none of the above
     *
     * The method is the same as taking the determinant of ABC, but includes
     * additional logic and precision arithmetic to ensure the above hold true.  We can also
     * say teh invariants below hold.
     *
     * Assert the following invariants:
     *      (1) Result == 0 iff two points are the same
     *      (2) ccw(a, b, c) == ccw(b, c, a) for all a, b, c
     *      (3) ccw(a, b, c) == -ccw(c, b, a) for all a , b, c
     */
    public static int expensiveCCW( Vector3D a, Vector3D b, Vector3D c ) {

        // Check Equality
        if ( a.equals(b) || b.equals(c) || c.equals(a) ) return 0;

        // Handle the Edge case of nearby and nearly antipodal points.
        double sab = (a.dotProduct(b) > 0) ? -1 : 1;
        double sbc = (b.dotProduct(c) > 0) ? -1 : 1;
        double sca = (c.dotProduct(a) > 0) ? -1 : 1;

        Vector3D vab = Vector3D.add(a, Vector3D.multiply(b, sab));
        Vector3D vbc = Vector3D.add(b, Vector3D.multiply(c, sbc));
        Vector3D vca = Vector3D.add(c, Vector3D.multiply(a, sca));

        double dab = vab.norm2();
        double dbc = vbc.norm2();
        double dca = vca.norm2();

        // Sort the difference vectors to find the longest edge, and use the
        // opposite vertex as the origin. If two difference vectors are the same length,
        // we break ties deterministically to ensure that the symmetry properties are true.

        double sign;
        if (dca < dbc || (dca == dbc && a.lessThan(b))) {
            if (dab < dbc || (dab == dbc && a.lessThan(c))) {
                // The "sab" factor converts A +/- B into B +/- A.
                sign = Vector3D.crossProd(vab, vca).dotProduct(a) * sab; // BC is longest
                // edge
            } else {
                sign = Vector3D.crossProd(vca, vbc).dotProduct(c) * sca; // AB is longest
                // edge
            }
        } else {
            if (dab < dca || (dab == dca && b.lessThan(c))) {
                sign = Vector3D.crossProd(vbc, vab).dotProduct(b) * sbc; // CA is longest
                // edge
            } else {
                sign = Vector3D.crossProd(vca, vbc).dotProduct(c) * sca; // AB is longest
                // edge
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
        return ccw;
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
