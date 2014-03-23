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
 * Intersection utils provides a set of utilities for determining the intersection between
 * two line segments (geodesics) with direct applications in determining the relationships
 * between shapes and a geodesic polygon.
 *
 * The math for intersection utilities is based in direction vectors. Therefore,
 * geographic points should be converted to Vectors before using these methods.
 *
 * resource: https://code.google.com/p/s2-geometry-library/source/browse/geometry/s2edgeutil.h
 *
 * Last Modified: 3/6/14
 */
public class IntersectUtils {

    /**
     *
     */
    public static Vector3D getIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Get intersection not yet implemented!");
    }

    /**
     *
     */
    public static boolean edgeOrVertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Edge or vertex crossing not yet implemented!");
    }

    /**
     *
     */
    public static boolean vertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Vertex crossing not yet implemented!");
    }

    /**
     *
     */
    public static boolean simpleIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Simple crossing not yet implemented!");
    }


    /**
     *
     */
    public static boolean robustIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Robust crossing not yet implemented!");
    }

    /// Helper Methods ///

    /**
     * Returns true if the points are listed in a counter-clockwise fashion with respect to
     * direction in the direction vector frame of reference (needs a better comment here!)
     */
    public boolean orderedCCW( Vector3D a, Vector3D b, Vector3D c, Vector3D o ) {

        int sum = 0;
        if ( simpleCCW(b, o, a) == true ) sum += 1;
        if ( simpleCCW(c, o, b) == true ) sum += 1;
        if ( simpleCCW(a, o, c) == true ) sum += 1; // this is wrong???
        return sum >= 2;
    }

    /**
     * Using a simple CCW - could not figure out a robust one
     */
    public boolean simpleCCW( Vector3D a, Vector3D b, Vector3D c ) {
        return VectorUtils.dotProduct( VectorUtils.crossProduct(c, a), b ) > 0;
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
    public int ccw( Vector3D a, Vector3D b, Vector3D c ) {

        // Check Equality
        if ( a.equals(b) || b.equals(c) || c.equals(a) ) return 0;

        // Handle the Edge case of nearby and nearly antipodal points.
        double sab = (VectorUtils.dotProduct(a, b) > 0) ? -1 : 1;
        double sbc = (VectorUtils.dotProduct(b, c) > 0) ? -1 : 1;
        double sca = (VectorUtils.dotProduct(c, a) > 0) ? -1 : 1;

        Vector3D vab = VectorUtils.sum(a, VectorUtils.multiply(b, sab));
        Vector3D vbc = VectorUtils.sum(b, VectorUtils.multiply(c, sbc));
        Vector3D vca = VectorUtils.sum(c, VectorUtils.multiply(a, sca));

        double dab = VectorUtils.norm2(vab);
        double dbc = VectorUtils.norm2(vbc);
        double dca = VectorUtils.norm2(vca);

        // Sort the difference vectors to find the longest edge, and use the
        // opposite vertex as the origin. If two difference vectors are the same length,
        // we break ties deterministically to ensure that the symmetry properties are true.

        double sign;
        if (dca < dbc || (dca == dbc && VectorUtils.mag(a) < VectorUtils.mag(b))) { // using mags because I am not sure if you can do direct comparison
            if (dca < dbc || (dab == dbc && VectorUtils.mag(a) < VectorUtils.mag(c))) {
                sign = VectorUtils.dotProduct(VectorUtils.crossProduct(vab, vca), a) * sab; // BC is the longest edge
            } else {
                sign = VectorUtils.dotProduct(VectorUtils.crossProduct(vca, vbc), c) * sca;
            }
        } else {
            if ( dab < dca || (dab == dca && VectorUtils.mag(b) < VectorUtils.mag(c))) {
                sign = VectorUtils.dotProduct(VectorUtils.crossProduct(vbc, vab), b) * sbc;
            } else {
                sign = VectorUtils.dotProduct(VectorUtils.crossProduct(vca, vbc), c) * sca;
            }
        }

        if (sign > 0) return 1;
        if (sign < 0) return -1;

        return 0; // else case? still needs to handle coplanar edge case with planar ccw

    }
}
