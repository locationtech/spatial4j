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
import com.spatial4j.core.context.SpatialContext;
import org.junit.*;

import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

/**
 * Test static methods for basic 3-component vector operations
 */
public class Vector3DUtilsTest extends RandomizedTest {

    // needed for creating regular points
    private SpatialContext ctx;

    /**
     * Setup before test
     */
    @Before
    public void setUp() {
        ctx = SpatialContext.GEO;
    }

    /**
     * Tear down after test
     */
    @After
    public void tearDown() {}

    /**
     * Test addition/subtraction of a few random points
     */
    @Test
    public void testAddSubtract() {

        for ( int i = 0; i <= randomIntBetween(10, 20); i++ ) {
            Vector3D a = new Vector3D( randomDouble(), randomDouble(), randomDouble() );
            Vector3D b = new Vector3D( randomDouble(), randomDouble(), randomDouble() );

            Vector3D result = Vector3DUtils.difference( Vector3DUtils.sum(a, b), b);

            assertEquals( a.getX(), result.getX(), 0.0001);
            assertEquals( a.getY(), result.getY(), 0.0001);
            assertEquals( a.getZ(), result.getZ(), 0.0001);
        }

    }

    /**
     * test Multiplication/division of a few random points
     */
    @Test
    public void testMultiplyDivide() {

        for ( int i = 0; i <= randomIntBetween(10, 20); i++ ) {
            Vector3D a = new Vector3D( randomDouble(), randomDouble(), randomDouble() );
            double k = randomDouble();

            Vector3D result = Vector3DUtils.multiply( Vector3DUtils.multiply(a, k), 1/k);

            assertEquals( a.getX(), result.getX(), 0.0001);
            assertEquals( a.getY(), result.getY(), 0.0001);
            assertEquals( a.getZ(), result.getZ(), 0.0001);
        }

    }

    /**
     * Test Expected magnitude (done with constants)
     */
    @Test
    public void testMagnitude() {

        // Creating a bunch of unit vectors from random points.
        for ( int i = 0; i <= randomIntBetween(10, 20); i++ ) {
            Point p = new PointImpl( randomInt(20), randomInt(20), ctx );
            Vector3D v = TransformUtils.toVector(p);
            assertEquals( Vector3DUtils.mag(v), 1, 0.0001);
        }
    }

    /**
     * Test Create Unit Vector
     */
    @Test
    public void testUnitVector() {

        // Create a bunch of random vectors, create unit vectors from those
        for ( int i = 0; i <= randomIntBetween(10, 20); i++ ) {
            Vector3D v = new Vector3D( randomDouble(), randomDouble(), randomDouble() );
            Vector3D uv = Vector3DUtils.unitVector(v);

            assertEquals( Vector3DUtils.mag(uv), 1, 0.0001);

            // Derive the original vector
            double mag = Vector3DUtils.mag(v);
            Vector3D v1 = new Vector3D( uv.getX()*mag, uv.getY()*mag, uv.getZ()*mag );

            // Approx equals
            assertEquals(v1.getX(), v.getX(), 0.0001);
            assertEquals(v1.getY(), v.getY(), 0.0001);
            assertEquals(v1.getZ(), v.getZ(), 0.0001);

        }
    }

    /**
     * Test expected dot product (done with constants)
     */

    /**
     * Test expected cross product (done with constants)
     */

    // Other methods used will also need to be tested in the future as they get added
    // add them here...



}
