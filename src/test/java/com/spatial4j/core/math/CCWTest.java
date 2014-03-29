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

import com.spatial4j.core.math.CCW;
import com.spatial4j.core.math.Vector3DUtils;
import com.spatial4j.core.shape.Vector3D;

import com.carrotsearch.randomizedtesting.RandomizedTest;

import com.spatial4j.core.TestLog;
import com.spatial4j.core.context.SpatialContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert.*;

/**
 * Test cases for determining consistent orientation of direction
 * vectors in a shape
 */
public class CCWTest extends RandomizedTest {

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
     * Test Planar Ordered CCW Method
     */
    public void testPlanarOrderedCCW() {

    }

    /**
     * Test Planar CCW Method
     */
    public void testPlanarCCW() {

    }

    /**
     * Test Ordered CCW Method
     */
    public void testOrderedCCW() {

    }

    /**
     * Test Expensive CCW Method
     */
    public void testExpensiveCCW() {

    }

    /**
     * Test Robust CCW Method
     */
    @Test
    public void testRobustCCW() {

        // Test Case from S2 Lib
        Vector3D a = new Vector3D(0.72571927877036835, 0.46058825605889098, 0.51106749730504852);
        Vector3D b = new Vector3D(0.7257192746638208, 0.46058826573818168, 0.51106749441312738);
        Vector3D c = new Vector3D(0.72571927671709457, 0.46058826089853633, 0.51106749585908795);
        assertTrue(CCW.robustCCW(a, b, c) != 0);

    }

    /**
     * Make a Random Set of Oriented Points
     */
    private Vector3D[] make_random_point_set( int numPoints, boolean oriented, boolean ccw ) {

        // Create a New Random GeoCircle (just needs to be a valid circle)
        ctx.makeCircle(45, 45, 10);

        // Split the circle into random segments

        // pick random points on the circle in each bin

        // convert to 3D points

        // return point set



    }

}
