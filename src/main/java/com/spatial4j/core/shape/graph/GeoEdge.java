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

package com.spatial4j.core.shape.graph;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.math.TransformUtils;
import com.spatial4j.core.math.VectorUtils;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.GeocentricPoint;
import com.spatial4j.core.shape.impl.BufferedLine;

/**
 * A GeoEdge represents a geodesic (line) between two 3D geocentric points in space
 * as part of a GeoGraph which is the internal representation of a Geodetic Polygon in Spatial4j
 */
public class GeoEdge {

    private GeocentricPoint p1;
    private GeocentricPoint p2;
    private double geodesicDist;

    /**
     * Keep default constructor private!!
     */
    private GeoEdge() {}

    /**
     * Construct a geodesic edge from 2 Points
     */
    public GeoEdge( Point p1, Point p2 ) {
        init(p1, p2);
    }

    /**
     * Reset the Edge - Good for recycling of classes??
     */
    public void reset( Point point1, Point point2 ) {
        init( point1, point2 );
    }

    /**
     * Initialize Edge from 2 Geocentric Points
     */
    private void init( Point point1, Point point2 ) {

        this.p1 = TransformUtils.toGeocentric(point1);
        this.p2 = TransformUtils.toGeocentric(point2);
        this.geodesicDist = DistanceUtils.distHaversineRAD( point1.getX(), point1.getY(), point2.getX(), point2.getY() );

    }

    /**
     * Access Point 1 of the Edge
     */
    public GeocentricPoint getP1() {
        return this.p1;
    }

    /**
     * Access Point 2 of the Edge
     */
    public GeocentricPoint getP2() {
        return this.p2;
    }
}
