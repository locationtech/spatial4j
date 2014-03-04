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
import org.junit.*;

import com.spatial4j.core.shape.Vector3D;

/**
 * Test static methods for basic 3-component vector operations
 */
public class VectorUtilsTest extends RandomizedTest {

    /**
     * Setup before test
     */
    @Before
    public void setUp() {}

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

            Vector3D result = VectorUtils.difference( VectorUtils.sum(a, b), b);

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

            Vector3D result = VectorUtils.multiply( VectorUtils.multiply(a, k), 1/k);

            assertEquals( a.getX(), result.getX(), 0.0001);
            assertEquals( a.getY(), result.getY(), 0.0001);
            assertEquals( a.getZ(), result.getZ(), 0.0001);
        }

    }

    /**
     * Test Expected magnitude (done with constants)
     */

    /**
     * Test expected dot product (done with constants)
     */

    /**
     * Test expected cross product (done with constants)
     */

    // Other methods used will also need to be tested in the future as they get added
    // add them here...



}
