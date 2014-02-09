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
 * A GeoEdge represents a geodesic (line) between two 3D geocentric points in space. The
 * GeoEdge is not an explicit representation of a geodesic, but is part of a GeoGraph
 * which is the internal representation of a Geodesic Polygon in Spatial4j. Uses haversine
 * for distance calculations by default.
 */
public class GeoEdge {

    // Store 2 Geocentric Points and the distance along the geodesic between them
    private GeocentricPoint pA;
    private GeocentricPoint pB;
    private double geodesicDist;

    private GeoEdge() {}

    /**
     * Construct a geodesic edge from 2 Geographic Points
     */
    public GeoEdge( Point pA, Point pB ) {
        init(pA, pB);
    }

    /**
     * Complete a full reset of the edge - enables the use of immutable data while also recycling objects in
     * memory allocation.
     */
    public void reset( Point point1, Point point2 ) {
        init( point1, point2 );
    }

    /**
     * Initialize Edge from 2 Geocentric Points
     */
    private void init( Point pointA, Point pointB ) {

        this.pA = TransformUtils.toGeocentric(pointA);
        this.pB = TransformUtils.toGeocentric(pointB);
        this.geodesicDist = DistanceUtils.distHaversineRAD( pointA.getX(), pointB.getY(), pointA.getX(), pointB.getY() );
    }

    /**
     * Access Point 1 of the Edge as a geocentric point (3D)
     */
    public GeocentricPoint getGP1() {
        return this.pA;
    }

    /**
     * Access Point 1 of the edge as a geographic (lat/lon) point
     */
    public Point getP1() {
        return TransformUtils.toGeodetic(pA);
    }

    /**
     * Access Point 2 of the Edge as a geocentric point (3D)
     */
    public GeocentricPoint getGP2() {
        return this.pB;
    }

    /**
     * Access Point 2 of the edge as a geographic (lat/lon) point
     */
    public Point getP2() {
        return TransformUtils.toGeodetic(pB);
    }

    /**
     * Access the distance between the two points
     */
    public double getDistance() {
        return geodesicDist;
    }
}
