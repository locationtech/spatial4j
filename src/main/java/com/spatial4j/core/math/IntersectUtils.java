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
 * Intersection utilities provides a set of methods for computing the intersections between
 * two geodesics, where each geodesic is a geodesic line segment defined by two direction cosine
 * vectors.
 *
 * In particular, this class provides utilities for grabbing the intersection point itself,
 * boolean methods for determining if an edge or vertex intersects, and computing both
 * simple and robust intersections.
 *
 * The intersection methods in this class were adapted for use with Spatial4j shapes from the following projects:
 *      S2-Java: https://code.google.com/p/s2-geometry-library-java/source/
 *      S2-C++: https://code.google.com/p/s2-geometry-library/s
 *
 * Last Updated: 3/25/14
 */
public class IntersectUtils {

    // private constructor
    private IntersectUtils() {}

    /**
     * Determines the intersection point between two geodesics - AB and CD. Will only
     * return if robustCrossing returns true (internal). We can make the following statement.
     *
     * Assert the following:
     *      (1) getIntersection(b, a, c, d) == getIntersection(a, b, d, c)
     *      (2) getIntersection(c, d, a, b) == getIntersection(a, b, c, d)
     *
     * **Numerical Robustness: The intersection point x is guaranteed to be close
     * to AB and CD. However, if edges intersect at very small angles, then X might not be
     * as close to the intersection point. Can set a tolerance for this.
     */
    public static Vector3D getIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        Vector3D a_norm = Vector3D.normalize(robustCrossProd(a, b));
        Vector3D b_norm = Vector3D.normalize(robustCrossProd(c, d));
        Vector3D x = Vector3D.normalize(robustCrossProd(a_norm, b_norm));

        // Check the intersection point is on the right side of the sphere. Since all
        // vertices are of unit length, edges are less than 180 degrees, needs to do this
        if ( x.dotProduct(Vector3D.add(Vector3D.add(a,b), Vector3D.add(c,d))) < 0 ) {
            x = Vector3D.multiply(x, -1);
        }

        // Calculation above is enough to ensure that x is within some intersection tolerance
        // of the great cirlces through a,b, and c,d. However, if these circles are close to parallel
        // then it is possible that x does not lie between the endpoitn so fthe given line segments
        if ( CCW.orderedCCW(a, x, b, a_norm ) && CCW.orderedCCW(c, x, d, b_norm)) return x;

        // Find the acceptable endpoint closest to x and return it. An endpoint is
        // acceptable if it lies between the endpoints of the other line segment.
        double dmin = 10;
        Vector3D vmin = x;

        if ( CCW.orderedCCW(c, a, d, b_norm)) replaceIfCloser(x, a, dmin, vmin);
        if ( CCW.orderedCCW(c, b, d, b_norm)) replaceIfCloser(x, b, dmin, vmin);
        if ( CCW.orderedCCW(a, c, b, a_norm)) replaceIfCloser(x, c, dmin, vmin);
        if ( CCW.orderedCCW(a, c, b, a_norm)) replaceIfCloser(x, c, dmin, vmin);

        return vmin;
    }

    /**
     * Convenience method that optimizes for cases where all four vertices are distinct.
     */
    public static boolean edgeOrVertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        int crossing = robustCrossing(a, b, c, d);
        if (crossing < 0) return false;
        if (crossing > 0) return true;
        return vertexIntersection(a, b, c, d);
    }

    /**
     * Convenience method that optimizes for cases where oen or more vertices is equivalent.
     */
    public static boolean vertexIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        // If A == B or C == D there is no intersection. Optimization point.
        if ( a == b || c == d ) return false;

        // if any other pair of vertices is equal, there is a crossing iff orderedCCW indicates
        // that edge AB is further CCW around the shared vertex (either A or B) than edge CD, starting
        // from an arbitrary fixed reference point.
        if ( a == d ) return CCW.orderedCCW(a.ortho(), c, b, a);
        if ( b == c ) return CCW.orderedCCW(b.ortho(), d, a, b);
        if ( a == c ) return CCW.orderedCCW(a.ortho(), d, b, a);
        if ( b == d ) return CCW.orderedCCW(b.ortho(), c, a, b);

        // vertex crossing called with four distinct vertices (here they log fatal?)
        return false;
    }

    /**
     * Determines if there is an intersection between AB and CD. Returns true at a point that
     * is interior to both geodescics.
     *
     * Properties:
     *      (1) simpleCrossing(b, a, c, d) == simpleCrossing(a, b, c, d)
     *      (2) simpleCrossing(c, d, a, b) == simpleCrossing(a, b, c, d)
     */
    public static boolean simpleIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        Vector3D ab = a.crossProd(b);
        double acb = -1*ab.dotProduct(c);
        double bda = ab.dotProduct(d);

        if (acb * bda <= 0) return false;

        Vector3D cd = c.crossProd(d);
        double cbd = -1 * cd.dotProduct(b);
        double dac = cd.dotProduct(a);

        return (acb * cbd > 0) && (acb * dac > 0);

    }

    /**
     * Determine if the edges AB and CD intersect. Does this by arbitrarily classifying points
     * as being on one side or the other (such that robustCCW can be applied). Returns +1 if there
     * is a crossing, -1 f no crossing, and 0 if any two vertices are the same. Returns 0 or -1 if
     * either edge is degenerate.
     *
     * Assert the following:
     *      (1) robustCrossing(b, a, c, d) == robustCrossing(a, b, c, d)
     *      (2) robustCrossing(c, d, a, b) == robustCrossing(a, b, c, d)
     *      (3) robustCrossing(a, b, c, d) == 0 if a == c a == d b == c and b ==c
     *      (4) robustCrossing(a, b, c, d) <= 0 if a == b or c == d
     *
     */
    public static int robustCrossing( Vector3D a, Vector3D b, Vector3D c, Vector3D d) {

        Vector3D aXB = a.crossProd(b);
        int acb = -1*CCW.robustCCW(a, b, c, aXB );
        int bda = CCW.robustCCW(a, b, d, aXB );

        // If any two vertices are the same, the result is degenerate.
        if ((bda & acb) == 0) {
            return 0;
        }

        // If ABC and BDA have opposite orientations (the most common case),
        // there is no crossing.
        if (bda != acb) {
            return -1;
        }

        // Otherwise we compute the orientations of CBD and DAC, and check whether
        // their orientations are compatible with the other two triangles.
        Vector3D cXD = c.crossProd(d);
        int cbd = -1 *CCW.robustCCW(c, d, b, cXD);
        if (cbd != acb) {
            return -1;
        }

        int dac = CCW.robustCCW(c, d, a, cXD);
        return (dac == acb) ? 1 : -1;
    }

    /**
     * Helper method - replace point if I have found a better intersection point.
     */
    private static void replaceIfCloser( Vector3D a, Vector3D b, double dmin, Vector3D vmin ) {

        double d2 = Vector3D.minus(a, b).norm2();
        if ( d2 < dmin || (d2 == dmin && b.lessThan(vmin))) {
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
    private static Vector3D robustCrossProd( Vector3D a, Vector3D b ) {
        Vector3D x = (Vector3D.add(a, b)).crossProd(Vector3D.minus(b, a));
        if ( !x.equals(new Vector3D(0, 0, 0))) return x;
        return a.ortho();
    }
}
