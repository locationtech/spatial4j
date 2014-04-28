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

import java.util.List;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.math.IntersectUtils;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.GeoPolygon;

import com.spatial4j.core.math.TransformUtils;
import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.impl.PointImpl;

import com.spatial4j.core.shape.impl.RealGeoRange;

/**
 * Development notes
 * JTS currently uses monotone chains (i.e. chains that are strictly increasing
 * or decreasing) to optimize the computation. This is nice because you only have to search
 * intersections between these monotone chains. However, I think this is something I don't quite
 * want to implement yet - it would give speed, but the +/- pole wrapping issues could get
 * complicated in this scenario
 */

/**
 * Computes the relationship between a geographic (lat/lon) point
 * and a geodesic polygon
 */
public class PointInGeoPolygon {

    private SpatialContext ctx = SpatialContext.GEO;
    private GeoPolygon polygon;
    private int crossings = 0;

    /**
     * Constructor for PointInGeoPolygon method
     */
    private PointInGeoPolygon() {}

    /**
     * Determine if the point p lies in the given polygon
     */
    public boolean relatePolygonToPoint( GeoPolygon polygon, Point p ) {

        crossings = 0;

        // Get my list of points from the polygon
        List< Point > pts = polygon.getBoundary().getVertices();

        // Create a set of points representing an infinite ray. In 2D, the ray would have
        // bounds -inf, +inf but in Geodesic we bound in x -180, 180 around the world
        Vector3D v1 = TransformUtils.toVector( new PointImpl(-180, p.getY(), ctx) );
        Vector3D v2 = TransformUtils.toVector( new PointImpl(180, p.getY(), ctx) );

        // Create a new GeoRange where the latitude of point p represents the bounds of the interval
        RealGeoRange interval = new RealGeoRange(p.getY(), p.getY());

        // Initialize Vectors
        Vector3D v3 = null;
        Vector3D v4 = null;

        // Kill the tree, just iterate over every segment first
        for (int i = 0; i <= pts.size(); i++) {

            // Check the implicit closing
            if (i == pts.size()) {
                v3 = TransformUtils.toVector(pts.get(i));
                v4 = TransformUtils.toVector(pts.get(0));
            } else {
                v3 = TransformUtils.toVector(pts.get(i));
                v4 = TransformUtils.toVector(pts.get(i+1));
            }

            // Check intersection
            if ( IntersectUtils.robustCrossing(v1, v2, v3, v4) == 1 ) {
                crossings++;
            }
        }

        // If the number of crossings is odd, (crossings %2 = 2) return true. else return false
        if ((crossings % 2) == 1) {
            return true;
        }
        return false;
    }
}