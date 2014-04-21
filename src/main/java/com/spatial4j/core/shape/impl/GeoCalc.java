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

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;

import com.spatial4j.core.shape.GeoLoop;

import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.impl.RectangleImpl;

/**
 * Methods for Calculating Features of Geodesic Polygons
 * This class is mostly designed for testing to keep
 */
public class GeoCalc {

    // Spatial Context Info
    private static SpatialContext ctx = SpatialContext.GEO;

    private GeoCalc() {}

    // Compute Bounding Box Using Pairwise Latitude and Longitude Spans
    public static Rectangle computeLoopBBox( GeoLoop loop ) {

        // Expand an R1 and S1 range
        RealGeoRange latRange = RealGeoRange.empty();
        UnitGeoRange lonRange = UnitGeoRange.empty();

        // For each point in the loop, expand the range
        for ( int i = 0; i < loop.getVertices().size(); i++ ) {

            // Compute temporary ranges from point to union
            RealGeoRange r = RealGeoRange.fromPoint( loop.getVertices().get(i).getX() );
            UnitGeoRange u = UnitGeoRange.fromPoint( loop.getVertices().get(i).getY() );

            // Union new point ranges with existing ranges
            latRange.union(r);
            lonRange.union(u);

        }

        // Create a new bounding box from each range
        return new RectangleImpl( latRange.getMin(), latRange.getMax(), lonRange.getMin(), latRange.getMax(), ctx );
    }
}