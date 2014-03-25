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
 * Test cases for conversions between unit vector/direction vector and
 * geographic coordinates
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
    void testPlanarOrderedCCW() {

    }

    /**
     * Test Planar CCW Method
     */
    void testPlanarCCW() {

    }

    /**
     * Test Ordered CCW Method
     */
    void testOrderedCCW() {

    }

    /**
     * Test Expensive CCW Method
     */
    void testExpensiveCCW() {

    }

    /**
     * Test Robust CCW Method
     */
    void testRobustCCW() {

    }

}
