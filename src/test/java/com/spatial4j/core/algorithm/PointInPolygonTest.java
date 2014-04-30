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

package com.spatial4j.core.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.GeoPolygon;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test Suite for Computing teh Relationship between a point and a polygon
 */
public class PointInPolygonTest extends RandomizedTest {

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
     * Test a regular convex polygon (no boundary crossing, etc)
     */
    @Test
    public void testConvexPolygon() {

        List< Point > pts = new ArrayList< Point >();
        pts.add( new PointImpl(10, 20, ctx) );
        pts.add( new PointImpl(12, 30, ctx) );
        pts.add( new PointImpl(22, 35, ctx) );
        pts.add( new PointImpl(30, 25, ctx) );
        pts.add( new PointImpl(25, 15, ctx) );
        pts.add( new PointImpl(15, 14, ctx) );

        GeoPolygon polygon = new GeoPolygon(pts);

        // Test point in polygon
        Point testPt = new PointImpl(20, 20, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt) );

        // Test point outside polygon
        Point testPt2 = new PointImpl(0, 0, ctx);
        assertTrue(! PointInGeoPolygon.relatePolygonToPoint(polygon, testPt2) );

        // test Point on vertex
        Point testPt3 = new PointImpl(12, 30, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt3) );
    }

    /**
     * Test Convex Polygon
     */
    @Test
    public void testConcavePolygon() {

        List< Point > pts = new ArrayList< Point >();
        pts.add( new PointImpl(10, 20, ctx) );
        pts.add( new PointImpl(12, 30, ctx) );
        pts.add( new PointImpl(22, 23, ctx) );
        pts.add( new PointImpl(30, 25, ctx) );
        pts.add( new PointImpl(25, 15, ctx) );
        pts.add( new PointImpl(15, 14, ctx) );

        GeoPolygon polygon = new GeoPolygon(pts);

        // Test point in polygon
        Point testPt = new PointImpl(20, 20, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt) );

        // Test point outside polygon
        Point testPt2 = new PointImpl(0, 0, ctx);
        assertTrue(! PointInGeoPolygon.relatePolygonToPoint(polygon, testPt2) );

        // test Point on vertex
        Point testPt3 = new PointImpl(12, 30, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt3) );
    }

    /**
     * Polygon crossing a dateline
     */
    @Test
    public void testDatelineCrossingPolygon() {

        List< Point > pts = new ArrayList< Point >();
        pts.add( new PointImpl(-10, 20, ctx) );
        pts.add( new PointImpl(12, 30, ctx) );
        pts.add( new PointImpl(22, 23, ctx) );
        pts.add( new PointImpl(30, 25, ctx) );
        pts.add( new PointImpl(25, 15, ctx) );
        pts.add( new PointImpl(15, 14, ctx) );

        GeoPolygon polygon = new GeoPolygon(pts);

        // Test point in polygon
        Point testPt = new PointImpl(20, 20, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt) );

        // Test point on the other side of the dateline
        Point testPt1 = new PointImpl(-9, 20, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt1) );

        // Test point outside polygon
        Point testPt2 = new PointImpl(0, 0, ctx);
        assertTrue(! PointInGeoPolygon.relatePolygonToPoint(polygon, testPt2) );

        // test Point on vertex
        Point testPt3 = new PointImpl(12, 30, ctx);
        assertTrue( PointInGeoPolygon.relatePolygonToPoint(polygon, testPt3) );
    }

    // polygon pole wrapping case?? TODO

}
