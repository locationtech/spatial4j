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
public class TransformUtilsTest extends RandomizedTest {

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
     * Test point to Vector3D conversion
     */
    @Test
    public void testConversions() {

        double pi_2 = Math.PI/2;

        // Test Points: poles and dateline in radians and degrees
        Point dateline_deg = new PointImpl(65.0,  90.0, ctx);
        Point poles_deg = new PointImpl(180.0, 12.2, ctx);

        // Resulting points
        Point dateline_deg_post = TransformUtils.toPoint(TransformUtils.toVector(dateline_deg), ctx);
        Point poles_deg_post = TransformUtils.toPoint(TransformUtils.toVector(poles_deg), ctx);

        // Test Dateline Radian Point
        assertEquals( 90.0, DistanceUtils.toDegrees(dateline_deg_post.getY()), 0.0001); // delta is 0 for now
        assertEquals( 12.2, DistanceUtils.toDegrees(poles_deg_post.getY()), 0.0001);
        assertEquals( 180.0, DistanceUtils.toDegrees(poles_deg_post.getX()), 0.0001);
        assertEquals( 65.0, DistanceUtils.toDegrees(dateline_deg_post.getX()), 0.0001);

        // Test 10 random points for conversion accuracy
        for ( int i = 0; i <= 10; i++ ) {
            // Create a random lat/lng point
            double lat = randomInt(9);
            double lng = randomInt(9);

            Point p = new PointImpl(lng, lat, ctx );
            Point p_post = TransformUtils.toPoint( TransformUtils.toVector(p), ctx);

            assertEquals(lat, DistanceUtils.toDegrees(p_post.getY()), 0.0001);
            assertEquals(lng, DistanceUtils.toDegrees(p_post.getX()), 0.0001);

        }

    }

}
