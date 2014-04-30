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

package com.spatial4j.core.shape;

import com.spatial4j.core.shape.impl.PointImpl;

import java.util.List;
import java.util.ArrayList;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for implementation of a geodesic loop
 */
public class GeoLoopTest extends RandomizedTest {

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
     * Test construction of some valid GeoLoops
     */
    @Test
    public void testValidLoops() {

        // Construct loop with duplicate non-adjacent points
        List< Point > ptsA = new ArrayList<Point>();
        ptsA.add( new PointImpl( 30, 30, ctx ) );
        ptsA.add( new PointImpl( 30, 40, ctx ) );
        ptsA.add( new PointImpl( 40, 50, ctx ) );
        ptsA.add( new PointImpl( 60, 80, ctx ) );
        ptsA.add( new PointImpl( 40, 50, ctx ) );

        GeoLoop LoopA = new GeoLoop(ptsA, 1, false);
        assertTrue( !LoopA.isHole() );
        assertEquals( LoopA.depth(), 1, 0 );
        assertTrue( LoopA.isValid() );
        assertTrue( LoopA.getCanonicalFirstVertex().equals( new PointImpl(30, 30, ctx)));

        System.out.println( LoopA.toString() );

        // Construct loop with the minimum vertices
        List< Point > ptsB = new ArrayList<Point>();
        ptsB.add( new PointImpl( 0, 0, ctx ));
        ptsB.add( new PointImpl( 0, 40, ctx ));
        ptsB.add( new PointImpl( 40, 50, ctx ));

        GeoLoop LoopB = new GeoLoop(ptsB, 1, false);
        assertTrue( LoopB.isValid() );
        assertTrue(LoopB.getCanonicalFirstVertex().equals(new PointImpl(0, 0, ctx)));

        // Check a loop that crosses the dateline
        List< Point > ptsC = new ArrayList<Point>();
        ptsC.add( new PointImpl( 10, -20, ctx ));
        ptsC.add( new PointImpl( 10, 20, ctx ));
        ptsC.add( new PointImpl( 50, 0, ctx ));

        GeoLoop LoopC = new GeoLoop(ptsC, 1, false);
        assertTrue( LoopC.isValid() );


    }

    /**
     * Test Construction of loops with too few points
     */
    @Test(expected=IllegalStateException.class)
    public void testSmallPolygon() {

        // Construct loop with the minimum vertices
        List< Point > ptsB = new ArrayList<Point>();
        ptsB.add( new PointImpl( 0, 0, ctx ));
        ptsB.add( new PointImpl( 0, 40, ctx ));

        GeoLoop LoopB = new GeoLoop(ptsB, 1, false);
    }

    /**
     * Test construction of loops with intersecting edges
     */
    @Test(expected=IllegalStateException.class)
    public void testIntersectingEdgeLoop() {

        // Construct loop with the minimum vertices
        List< Point > ptsB = new ArrayList<Point>();
        ptsB.add( new PointImpl( 10, 50, ctx ));
        ptsB.add( new PointImpl( 20, 60, ctx )); // this edge intersects
        ptsB.add( new PointImpl( 10, 49, ctx )); // with this edge
        ptsB.add( new PointImpl( 20, 62, ctx ));

        GeoLoop LoopB = new GeoLoop(ptsB, 1, false);

    }

    /**
     * Test construction of loops with duplicate adjacent vertices
     */
    @Test(expected=IllegalStateException.class)
    public void testDuplicateVertices() {

        // Check a loop that crosses the dateline
        List< Point > ptsC = new ArrayList<Point>();
        ptsC.add( new PointImpl( 10, -20, ctx ));
        ptsC.add( new PointImpl( 10, 20, ctx ));
        ptsC.add( new PointImpl( 10, 20, ctx ));
        ptsC.add( new PointImpl( 50, 0, ctx ));

        GeoLoop LoopC = new GeoLoop(ptsC, 1, false);
    }
}
