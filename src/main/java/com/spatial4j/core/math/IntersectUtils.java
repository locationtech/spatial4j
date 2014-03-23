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



    }


    /**
     *
     */
    public static boolean robustIntersection(Vector3D a, Vector3D b, Vector3D c, Vector3D d) {
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

}
