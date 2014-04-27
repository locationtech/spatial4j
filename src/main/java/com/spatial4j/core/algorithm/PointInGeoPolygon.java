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
import java.util.ArrayList;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.GeoPolygon;

/**
 * Computes the relationship between a geographic (lat/lon) point
 * and a geodesic polygon
 */
public class PointInGeoPolygon {

    /// pssshhh no idea
    class MCSelecter extends MonotoneChainSelectAction
    {
        Coordinate p;

        public MCSelecter(Coordinate p)
        {
            this.p = p;
        }

        public void select(LineSegment ls)
        {
            testLineSegment(p, ls);
        }
    }

    private GeoPolygon polygon;
    private Bintree tree; // optimization from JTS
    private int crossings = 0;

    /**
     * Constructor for PointInGeoPolygon method
     */
    public PointInGeoPolygon( GeoPolygon polygon ) {
        this.polygon = polygon;
        buildIndex(); // build bintree for search
    }

    private void buildIndex() {

        tree = new Bintree();

        // List of vertices
        List< Point > pts = polygon.getBoundary().getVertices();
        List< MonotoneChains > mclist = ChainBuilder.getChains(pts);

        for ( int i = 0; i < mcList.size(); i++ ) {

            MonotoneChain mc = mcList.get(i);
            // get bounding box
            // get min latitude
            // get max latitude
            // insert the interval and the chain into the binary tree
        }
    }

    // Create a new interval

    public boolean isInside( Point p ) {

        crossings = 0;

        // Test all segments intersected by the ray from pt in positive x direction
        //Rectangle rayRectangle = new Rectangle( // min x, // max x, point get y, point get y);

        // Set y as the min and max in the new interval

        // query the tree for the interval and will return a list of segs (not sure what the list entails

        // Select a new monotone chain from the point
        // over the segments, test the monotone chain against selecter , mc and ray env

        // If the number of crossings is odd, (crossings %2 = 2) return true. else return false


    }

    // Helper Method
    private void testMonotoneChain( Box rayEnv, MCSelecter selecter, MonotoneChain mc ) {

        mc.select(rayEnv, mcSelecter)
    }

    private void testSegment( Point p, Segment s) [
    }

    private void testLineSegment(Coordinate p, LineSegment seg) {
        double xInt;  // x intersection of segment with ray
        double x1;    // translated coordinates
        double y1;
        double x2;
        double y2;

    /*
     *  Test if segment crosses ray from test point in positive x direction.
     */
        Coordinate p1 = seg.p0;
        Coordinate p2 = seg.p1;
        x1 = p1.x - p.x;
        y1 = p1.y - p.y;
        x2 = p2.x - p.x;
        y2 = p2.y - p.y;

        if (((y1 > 0) && (y2 <= 0)) ||
                ((y2 > 0) && (y1 <= 0))) {
        /*
         *  segment straddles x axis, so compute intersection.
         */
            xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
            //xsave = xInt;
        /*
         *  crosses ray if strictly positive intersection.
         */
            if (0.0 < xInt) {
                crossings++;
            }
        }
    }

}





}
