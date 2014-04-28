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

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.GeoPolygon;
import com.spatial4j.core.shape.Rectangle;

import java.util.List;
import java.util.ArrayList;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PolygonBBoxTest extends RandomizedTest {

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
     * Construct a polygon whose bounding box is itself
     */
    @Test
    public void testBasicBox() {

        List< Point > pts = new ArrayList<Point>();

        Point p1 = new PointImpl(50, 45, ctx);
        Point p2 = new PointImpl(60, 45, ctx);
        Point p3 = new PointImpl(50, 30, ctx);
        Point p4 = new PointImpl(60, 30, ctx);

        pts.add(p1);
        pts.add(p2);
        pts.add(p3);
        pts.add(p4);

        // Build a polygon
        GeoPolygon polygon = new GeoPolygon(pts);

        // Compute bounding box
        Rectangle rectangle = polygon.getBoundingBox();

        assert( rectangle.getMinX() == 50 );
        assert( rectangle.getMaxX() == 60 );

    }



}
