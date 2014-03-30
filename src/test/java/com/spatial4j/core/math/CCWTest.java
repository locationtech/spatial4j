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

import java.util.List;
import java.util.ArrayList;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
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
    @Test
    public void testPlanarOrderedCCW() {
    }

    /**
     * Test Planar CCW Method
     */
    @Test
    public void testPlanarCCW() {

    }

    /**
     * Test Ordered CCW Method
     */
    @Test
    public void testOrderedCCW() {

    }

    /**
     * Test Expensive CCW Method
     */
    @Test
    public void testExpensiveCCW() {

        // Test Case from S2 Lib
        Vector3D a = new Vector3D(0.72571927877036835, 0.46058825605889098, 0.51106749730504852);
        Vector3D b = new Vector3D(0.7257192746638208, 0.46058826573818168, 0.51106749441312738);
        Vector3D c = new Vector3D(0.72571927671709457, 0.46058826089853633, 0.51106749585908795);
       // assertTrue(CCW.expensiveCCW(a, b, c) != 0);

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

        // Check CCW returns true
        assertTrue(CCW.robustCCW(a, b, c, Vector3DUtils.crossProduct(a, b)) != 0);
        assertTrue(CCW.robustCCW(a, b, c) != 0);

    }

    /**
     * Make a Random Set of Oriented Points
     */
    private Vector3D[] make_random_point_set( int numPoints, boolean oriented, boolean ccw ) {

       // Picking a Non Random Lat Lon Point to start (boston -
        double x = 0; // some rand coord
        double y = 0; // some rand coord
        double r = 10; // fixed radius - doesn't matter becasue we will use direction vectors anyway


        // just kidding - do we even need the circle? its just a container for information
        // I already have

        // Pick four random points in the quadrants of the circle
        List< Point > pointArr = new ArrayList< Point >(4);

        // Quadrant 1
        double alpha = randomAngle(0.0, 90.0);
        Point a = new PointImpl( r*Math.cos(alpha), r*Math.sin(alpha), ctx);
        pointArr.add(a);

        // Quadrant 2
        double beta = randomAngle(90.0, 180.0);
        Point b = new PointImpl( r*Math.cos(beta), r*Math.sin(beta), ctx);
        pointArr.add(b);

        // Quadrant 3
        double gamma = randomAngle(180.0, 270.0);
        Point c = new PointImpl( r*Math.cos(gamma), r*Math.sin(gamma), ctx);
        pointArr.add(c);

        // Quadrant 4
        double chi = randomAngle(270.0, 360.0);
        Point d = new PointImpl( r*Math.cos(chi), r*Math.sin(chi), ctx);
        pointArr.add(d);

        // Declare 3 indices


        // If no orientation, pick three random indices
        if ( !oriented ) {

        }

        // pick random points on the circle in each bin

        // convert to 3D points

        // return point set

         return null;

    }

    /**
     * Helper method - pick a random angle within range and convert to degrees
     */
    double randomAngle( double lower, double upper ) {
        double angle = lower + (Math.random() * (upper - lower + 1));
        return DistanceUtils.toRadians(angle);
    }

}
