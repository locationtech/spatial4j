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

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.Vector3D;

import com.spatial4j.core.TestLog;
import com.spatial4j.core.context.SpatialContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert.*;

/**
 * Test cases for determining intersection points
 */
public class IntersectionUtilsTest extends RandomizedTest {

    /**
     * Helper test class - holds points for computing intersections
     */
    private class GeodesicSegments {

        // Data
        Vector3D Va;
        Vector3D Vb;
        Vector3D Vc;
        Vector3D Vd;

        // Construct a GeodesicSegments object from points
        public GeodesicSegments( Point Pa, Point Pb, Point Pc, Point Pd ) {

            // Convert to initialize
            Va = TransformUtils.toVector(Pa);
            Vb = TransformUtils.toVector(Pb);
            Vc = TransformUtils.toVector(Pc);
            Vd = TransformUtils.toVector(Pd);

        }

        /**
         * Get Various Data Points
         */
        public Vector3D getA() {
            return this.Va;
        }

        public Vector3D getB() {
            return this.Vb;
        }

        public Vector3D getC() {
            return this.Vc;
        }

        public Vector3D getD() {
            return this.Vd;
        }
    }


    // Required for backwards conversion
    private SpatialContext ctx;

    /**
     * Setup Spatial Context (Geodesic) Prior to running
     * the test suite
     */
    @Before
    public void setUp() {
        ctx = SpatialContext.GEO;
    }

    @After
    public void tearDown() {}

    /**
     * Test get intersection method
     */
    public void testGetIntersection() {

        // Case 1: Randomized Across Points

    }

    /**
     * Test Edge or vertex crossing method
     */
    public void testEdgeOrVertexMethod() {

    }

    /**
     * Test vertex crossing method
     */
    public void testVertexCrossing() {

    }

    /**
     * Test simple intersection method
     */
    @Test
    public void testSimpleIntersection() {

        Vector3D a, b, c, d;

        // Two Regular Edges that Cross
        a = new Vector3D( 1, 2, 1);
        b = new Vector3D( 1, -3, 0.5);
        c = new Vector3D( 1, -0.5, -3);
        d = new Vector3D( 0.1, 0.5, 3);

        assertEquals( IntersectUtils.robustCrossing( a, b, c, d), 1 );
        assertEquals( IntersectUtils.robustCrossing( a, c, b, d), -1 );

        // Two Regular Edges that Cross on antipodal points
        a = Vector3DUtils.normalize( new Vector3D( 1, 2, 1) );
        b = Vector3DUtils.normalize( new Vector3D( 1, -3, 0.5) );
        c = Vector3DUtils.normalize( new Vector3D( -1, 0.5, 3) );
        d = Vector3DUtils.normalize( new Vector3D( -0.1, -0.5, -3) );

        assertEquals( IntersectUtils.robustCrossing( a, b, c, d), 1 );
        assertEquals( IntersectUtils.robustCrossing( a, c, b, d), -1 );

        // Two Edges on the Same Great Cirlce
        a = Vector3DUtils.normalize( new Vector3D( 0, 0, -1 ) );
        b = Vector3DUtils.normalize( new Vector3D( 0, 1, 0 ) );
        c = Vector3DUtils.normalize( new Vector3D( 0, 1, 1 ) );
        d = Vector3DUtils.normalize( new Vector3D( 0, 0, 1 ) );

        assertEquals( IntersectUtils.robustCrossing( a, b, c, d), 1 );
        assertEquals( IntersectUtils.robustCrossing( a, c, b, d), -1 );
    }

    /**
     * Compute Random Intersecting Lines
     */



}
