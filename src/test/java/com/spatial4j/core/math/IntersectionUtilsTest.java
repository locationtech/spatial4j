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
     * Setup Spatial Context (Geodesic) Prior to running
     * the test suite
     */
    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    /**
     * Test Simple Intersection Method
     */
    @Test
    public void testSimpleIntersection () {

        Vector3D a, b, c, d;

        // Two Regular Edges that Cross
        a = new Vector3D( 1, 2, 1);
        b = new Vector3D( 1, -3, 0.5);
        c = new Vector3D( 1, -0.5, -3);
        d = new Vector3D( 0.1, 0.5, 3);

        assertTrue( IntersectUtils.simpleIntersection(a, b, c, d ) );
        assertTrue( !IntersectUtils.simpleIntersection( a, c, b, d ) );


        // Two Regular Edges that Cross on antipodal points
        a = Vector3DUtils.normalize( new Vector3D( 1, 2, 1) );
        b = Vector3DUtils.normalize( new Vector3D( 1, -3, 0.5) );
        c = Vector3DUtils.normalize( new Vector3D( -1, 0.5, 3) );
        d = Vector3DUtils.normalize( new Vector3D( -0.1, -0.5, -3) );

        assertTrue( !IntersectUtils.simpleIntersection(a, b, c, d ) );

        // Two Edges on the Same Great Cirlce
        a = Vector3DUtils.normalize( new Vector3D( 0, 0, -1 ) );
        b = Vector3DUtils.normalize( new Vector3D( 0, 1, 0 ) );
        c = Vector3DUtils.normalize( new Vector3D( 0, 1, 1 ) );
        d = Vector3DUtils.normalize( new Vector3D( 0, 0, 1 ) );

        assertTrue( !IntersectUtils.simpleIntersection(a, b, c, d ) );


    }

    /**
     * Test Robust Intersection Method
     */
    @Test
    public void testRobustIntersection() {

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

        assertEquals( IntersectUtils.robustCrossing( a, b, c, d), -1 );
        assertEquals( IntersectUtils.robustCrossing( a, c, b, d), -1 );

        // Two Edges on the Same Great Cirlce
        a = Vector3DUtils.normalize( new Vector3D( 0, 0, -1 ) );
        b = Vector3DUtils.normalize( new Vector3D( 0, 1, 0 ) );
        c = Vector3DUtils.normalize( new Vector3D( 0, 1, 1 ) );
        d = Vector3DUtils.normalize( new Vector3D( 0, 0, 1 ) );

        assertEquals( IntersectUtils.robustCrossing( a, b, c, d), -1 );
        assertEquals( IntersectUtils.robustCrossing( a, c, b, d), -1 );
    }
}
