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

import com.spatial4j.core.shape.Point;

import java.util.List;
import java.util.ArrayList;

/**
 * A GeoGraph is the internal representation of a geodesic polygon in Spatial4j. The vertices of a
 * GeoGraph are 3D points which define the boundary of the Polgyon and each edge represents
 * the connection between 2 vertices and the geodesic distance between them (and thus represends the geodesic
 * borders of the polygon).
 *
 * Because the shape of a GeoPolygon itself is immutable, a GeoGraph is also immutable once constructed
 * but can be reset from scratch if needed.
 */
public class GeoGraph {

    // Represent a graph internally as a list of edges
    private List< GeoEdge > edgeList;
    private Point[] points;

    private GeoGraph() {}

    public GeoGraph( Point[] points ) {
        init(points);
    }

    /**
     * Given a list of ordered points, create an edge for the pairs of points
     * in order creating a graph.
     * @param points
     */
    private void init( Point[] points ) {

        this.points = points;
        this.edgeList = new ArrayList<GeoEdge>(points.length -1); // n-1 edges in a bounding polygon for n points

        // For each point, create an edge between the two points
        for ( int i = 0; i < points.length; i++ ) {
            GeoEdge e = new GeoEdge( points[i], points[i+1] );
            this.edgeList.add(e);
        }

        // Connect the ends of the polygon
        GeoEdge connect = new GeoEdge( points[0], points[points.length-1] );

        // Check invariants
        isGeoGraph();
    }

    /**
     * Access the geograph edge list
     */
    public List< GeoEdge > getGeoEdges() {
        return this.edgeList;
    }

    /**
     * Access a unique vertices list in the edge list
     */
    public Point[] getPoints() {
        return this.points;
    }


    /**
     * GeoGraph Invariant Checking
     */

    /**
     * Check if this GeoGraph is a Valid GeoGraph:
     *  (1) No redundant input points
     *  (2) Internal Geometry is fully connected
     */
    public void isGeoGraph() {
        // check connected components
        // check for redundant vertices
    }

    /**
     * Graph Equality
     */
    @Override
    public boolean equals(Object other) {
        return false; // TODO implement this
    }

    /**
     * Graph Hash Code
     */
    @Override
    public int hashCode() {
        return 0; // TODO Implement this
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return "";
    }
}
