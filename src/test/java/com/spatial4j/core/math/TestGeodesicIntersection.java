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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the algorithm for computing the intersection between two geodesics
 */
public class TestGeodesicIntersection {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    /**
     * Test a case where the two geodesic segments have no intersection
     */
    @Test
    public void testNonIntersecting() {

    }

    /**
     * Test a case where the same segment is passed (still gets expected intersection result)
     */
    @Test
    public void testSameSegment() {

    }

    /**
     * Test the case where both lines lie along the same longitude
     * but do not intersect
     */
    @Test
    public void testSameLongNoIntersect() {

    }

    /**
     * Test the case where both lines lie along the same latitude but do not
     * intersect
     */
    @Test
    public void testSameLatNoIntersect() {

    }

    /**
     * Test the case where the two segments intersect at the pole
     */
    @Test
    public void testPoleIntersection() {

    }


}
