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

/**
 * Methods for Calculating Features of Geodesic Polygons
 * This class is mostly designed for testing to keep
 */
public class GeoCalc {

    // Spatial Context Info
    SpatialContext ctx = SpatialContext.GEO;

    private GeoCalc() {}


    // Compute Iterative Bounding Box Using Internal Coordinates
    // note - polygon should have some method called "convert to x"
    // to simplify the calls
    void computeBBox() {

        // convert lat/lon loop to dcos loop

        // Initialize Bounds for the Rectangle
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;

        int numVertices = 0; // dummy var

        // Create a new empty rectangle
        //RectangleImpl bbox = new RectangleImpl( minX, minY, maxX, maxY, ctx);

        // For each point in the loop, add the point to some reference
        for ( int i = 0; i <= numVertices; i++ ) {


        }


    }



    // Compute Bounding Box Using Pairwise Latitude and Longitude Spans



}


    // The previous vertex in the chain.
    private S2Point a;

    // The corresponding latitude-longitude.
    private S2LatLng aLatLng;

    // The current bounding rectangle.
    private S2LatLngRect bound;

    public RectBounder() {
        this.bound = S2LatLngRect.empty();
    }

    /**
     * This method is called to add each vertex to the chain. 'b' must point to
     * fixed storage that persists for the lifetime of the RectBounder.
     */
    public void addPoint(S2Point b) {
        // assert (S2.isUnitLength(b));

        S2LatLng bLatLng = new S2LatLng(b);

        if (bound.isEmpty()) {
            bound = bound.addPoint(bLatLng);
        } else {
            // We can't just call bound.addPoint(bLatLng) here, since we need to
            // ensure that all the longitudes between "a" and "b" are included.
            bound = bound.union(S2LatLngRect.fromPointPair(aLatLng, bLatLng));

            // Check whether the min/max latitude occurs in the edge interior.
            // We find the normal to the plane containing AB, and then a vector
            // "dir" in this plane that also passes through the equator. We use
            // RobustCrossProd to ensure that the edge normal is accurate even
            // when the two points are very close together.
            S2Point aCrossB = S2.robustCrossProd(a, b);
            S2Point dir = S2Point.crossProd(aCrossB, new S2Point(0, 0, 1));
            double da = dir.dotProd(a);
            double db = dir.dotProd(b);

            if (da * db < 0) {
                // Minimum/maximum latitude occurs in the edge interior. This affects
                // the latitude bounds but not the longitude bounds.
                double absLat = Math.acos(Math.abs(aCrossB.get(2) / aCrossB.norm()));
                R1Interval lat = bound.lat();
                if (da < 0) {
                    // It's possible that absLat < lat.lo() due to numerical errors.
                    lat = new R1Interval(lat.lo(), Math.max(absLat, bound.lat().hi()));
                } else {
                    lat = new R1Interval(Math.min(-absLat, bound.lat().lo()), lat.hi());
                }
                bound = new S2LatLngRect(lat, bound.lng());
            }
        }
        a = b;
        aLatLng = bLatLng;
    }

    /**
     * Return the bounding rectangle of the edge chain that connects the
     * vertices defined so far.
     */
    public S2LatLngRect getBound() {
        return bound;
    }
