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
}
