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


        // We use RobustCrossProd() to get accurate results even when two endpoints
        // are close together, or when the two line segments are nearly parallel.

        S2Point a_norm = S2::RobustCrossProd(a0, a1).Normalize();
        S2Point b_norm = S2::RobustCrossProd(b0, b1).Normalize();
        S2Point x = S2::RobustCrossProd(a_norm, b_norm).Normalize();

        // Make sure the intersection point is on the correct side of the sphere.
        // Since all vertices are unit length, and edges are less than 180 degrees,
        // (a0 + a1) and (b0 + b1) both have positive dot product with the
        // intersection point.  We use the sum of all vertices to make sure that the
        // result is unchanged when the edges are reversed or exchanged.

        if (x.DotProd((a0 + a1) + (b0 + b1)) < 0) x = -x;

        // The calculation above is sufficient to ensure that "x" is within
        // kIntersectionTolerance of the great circles through (a0,a1) and (b0,b1).
        // However, if these two great circles are very close to parallel, it is
        // possible that "x" does not lie between the endpoints of the given line
        // segments.  In other words, "x" might be on the great circle through
        // (a0,a1) but outside the range covered by (a0,a1).  In this case we do
        // additional clipping to ensure that it does.

        if (S2::OrderedCCW(a0, x, a1, a_norm) && S2::OrderedCCW(b0, x, b1, b_norm))
        return x;

        // Find the acceptable endpoint closest to x and return it.  An endpoint is
        // acceptable if it lies between the endpoints of the other line segment.
        double dmin2 = 10;
        S2Point vmin = x;
        if (S2::OrderedCCW(b0, a0, b1, b_norm)) ReplaceIfCloser(x, a0, &dmin2, &vmin);
        if (S2::OrderedCCW(b0, a1, b1, b_norm)) ReplaceIfCloser(x, a1, &dmin2, &vmin);
        if (S2::OrderedCCW(a0, b0, a1, a_norm)) ReplaceIfCloser(x, b0, &dmin2, &vmin);
        if (S2::OrderedCCW(a0, b1, a1, a_norm)) ReplaceIfCloser(x, b1, &dmin2, &vmin);

        DCHECK(S2::OrderedCCW(a0, vmin, a1, a_norm));
        DCHECK(S2::OrderedCCW(b0, vmin, b1, b_norm));
        return vmin;



    }

    /**
     *
     */
    public static boolean edgeOrVertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        int crossing = robustIntersection(a, b, c, d);
        if (crossing < 0) return false;
        if (crossing > 0) return true;
        return vertexIntersection(a, b, c, d);
    }

    /**
     *
     */
    public static boolean vertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        // If A == B or C == D there is no intersection. Optimization point.
        if ( a == b || c == d ) return false;

        // if any other pair of vertices is equal, there is a crossing iff orderedCCW indicates
        // that edge AB is further CCW around the shared vertex (either A or B) than edge CD, starting
        // from an arbitrary fixed reference point.
        if ( a == d ) return CCW.orderedCCW(Vector3DUtils.ortho(a), c, b, a);
        if ( b == c ) return CCW.orderedCCW(Vector3DUtils.ortho(b), d, a, b);
        if ( a == c ) return CCW.orderedCCW(Vector3DUtils.ortho(a), d, b, a);
        if ( b == d ) return CCW.orderedCCW(Vector3DUtils.ortho(b), c, a, b);

        // vertex crossing called with four distinct vertices (here they log fatal?)
        return false;
    }

    /**
     *
     */
    public static boolean simpleIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        Vector3D ab = Vector3DUtils.crossProduct(a, b);
        double acb = -1*Vector3DUtils.dotProduct(ab, c);
        double bda = Vector3DUtils.dotProduct(ab, d);

        if (acb * bda <= 0) return false;

        Vector3D cd = Vector3DUtils.crossProduct(c, d);
        double cbd = -1 * Vector3DUtils.dotProduct(cd, b);
        double dac = Vector3DUtils.dotProduct(cd, a);

        return (acb * cbd > 0) && (acb * dac > 0);

    }


    /**
     *
     */
    public static int robustIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
        throw new UnsupportedOperationException("Robust crossing not yet implemented!");
    }

    /// Helper Methods ///

    private int robustCrossing( Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        Vector3D cd = Vector3DUtils.crossProduct(c, d);
        int cbd = -1*CCW.expensiveCCW(c, d, b, cd); // sign... the long running problem with robust CCW???
        int acb = CCW.expensiveCCW(a, c, b);
        if ( cbd != acb ) return -1;

        int dac = CCW.expensiveCCW(c, d, a, cd); // sign... the long running problem with robust CCW???
        return (dac == acb) ? 1 : -1;
    }

    // replace if closer... no idea??
    // so C++ is passing by reference, i think this will work in java too. Will need to see.
    private void replaceIfCloser( Vector3D a, Vector3D b, double dmin, Vector3D vmin ) {

        double d2 = Vector3DUtils.norm2(Vector3DUtils.difference(a, b));
        if ( d2 < dmin || (d2 == dmin && Vector3DUtils.greaterThan(b, vmin))) {
            dmin = d2;
            vmin = b;
        }
    }

    /**
     * Robust Cross Product - The direction becomes unstable as (a+b) or (a-b)
     * approaches 0. Leads to scenarios where cross prod is not orthogonal to a or b
     * Easiest fix is to compute the cross product of (b+a) and (b-a). Mathematically, this is
     * twice the cross product of a and b but has the numerical advantage that (b+a) and
     * (b-a) are always orthogonal.
     */
    private Vector3D robustCrossProd( Vector3D a, Vector3D b ) {
        Vector3D x = Vector3DUtils.crossProduct(
                Vector3DUtils.sum(a, b),
                Vector3DUtils.difference(b, a));
        if ( !x.equals(new Vector3D(0, 0, 0))) return x;
        return Vector3DUtils.ortho(a);
    }
}
