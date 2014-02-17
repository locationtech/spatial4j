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
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.BufferedLine;

/**
 * A GeoEdge represents the geodesic between two geographic points in space and the shortest
 * distance between them. A GeoEdge is an implicit representation of a geodesic and part of
 * a GeoGraph - the internal representation of a Geodesic Polygon in Spatial4j. Uses
 * haversine distance calculations by default.
 */
public class GeoEdge {

    // Store 2 Geocentric and the distance along the geodesic between them
    private Point pA;
    private Point pB;
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
        this.pA = pointA;
        this.pB = pointB;
        this.geodesicDist = DistanceUtils.distHaversineRAD( pointA.getX(), pointB.getY(), pointA.getX(), pointB.getY() );
    }

    /**
     * Access Point 1 of the edge as a geographic (lat/lon) point
     */
    public Point getP1() {
        return this.pA;
    }

    /**
     * Access Point 2 of the edge as a geographic (lat/lon) point
     */
    public Point getP2() {
        return this.pB;
    }

    /**
     * Access the distance between the two points
     */
    public double getDistance() {
        return geodesicDist;
    }

    /**
     * Are two given geo edges equal?
     */
    @Override
    public boolean equals(Object other) {
        return equals(this, other);
    }


    /**
     * All GeoEdges should use this definition of equals
     */
    public static boolean equals( GeoEdge thiz, Object o ) {
        assert thiz != null;
        if ( thiz == o ) return true;
        if (!(o instanceof GeoEdge)) return false;

        GeoEdge e = (GeoEdge) o;

        if (!e.getP1().equals(thiz.getP1())) return false;
        if (!e.getP2().equals(thiz.getP2())) return false;
        if (Double.compare(e.getDistance(), thiz.getDistance()) != 0) return false;

        return true;
    }

    /**
     * HashCode Method for GeoEdge
     */
    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * All GeoEdges should use this definition of hashCode
     */
    public static int hashCode(GeoEdge thiz) {
        int result;
        long temp;
        temp = thiz.getP1().getX() != +0.0d ? Double.doubleToLongBits(thiz.getP1().getX()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = thiz.getP2().getY() != +0.0d ? Double.doubleToLongBits(thiz.getP2().getY()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = thiz.getDistance() != +0.0d ? Double.doubleToLongBits(thiz.getDistance()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));

        return result;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "GeoEdge{" +
               "point 1=(" + pA.getX() + " " + pB.getY() + "), " +
               "point 2=(" + pB.getX() + " " + pB.getY() + "), " +
               "distance=" + geodesicDist +
                '}';
    }
}
