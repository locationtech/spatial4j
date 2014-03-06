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
 * A GeoGraph is the internal representation of a geodesic polygon in Spatial4j. The vertices
 * of a GeoGraph are 2D points which define the boundary of the Polygon and each edge represents
 * the connection between these two points, weighted by the shortest-path distance between them.
 * The full polygon is a closed cycle, but this class also marks start and stop points.
 *
 * Because the shape of a GeoPolygon itself is immutable, a GeoGraph is also immutable once constructed
 * but can be reset from scratch if needed.
 *
 * Loop implementations and invariants taken from the s2Loop class in s2Geom
 */
public class GeoGraph {

    // Represent a graph internally as a list of GeoEdges (see GeoEdge Class)
    private List< GeoEdge > edgeList;

    private GeoGraph() {}

    public GeoGraph( Point[] points ) {
        initFromPoints(points);
    }

    /**
     * Given a list of ordered points, create an edge for the pairs of points, mark
     * the start and end points, and build the full graph.
     * @param points
     */
    private void initFromPoints( Point[] points ) {

        // Initialize edgeList
        this.edgeList = new ArrayList<GeoEdge>(points.length -1); // n-1 edges in a bounding polygon for n points

        // For each point, create an edge between the two points
        for ( int i = 0; i < points.length; i++ ) {
            GeoEdge e = new GeoEdge( points[i], points[i+1] );
            this.edgeList.add(e);
        }

        // Connect the ends of the polygon
        GeoEdge connect = new GeoEdge( points[0], points[points.length-1] );
        this.edgeList.add(connect);

        // Check invariants
        if ( !isGeoGraph() ) {
            throw new IllegalStateException("Invalid Construction of a GeoGraph!");
        }
        return;
    }

    /**
     * Access the edge list of the GeoGraph
     */
    public List< GeoEdge > getGeoEdges() {
        return this.edgeList;
    }

    /**
     * Access the unique list of vertices (points) in the edge list
     */
    public Point[] getPoints() {
        return getPointsFromEdges();
    }


    /**
     * Compute the non redundant set of points from the edge list
     */
    public Point[] getPointsFromEdges() {

        // Create a new list of points
        Point[] points = new Point[ this.edgeList.size() ];

        // Iterate through the edge list and copy over nr points
        for ( int i = 0; i < this.edgeList.size(); i++ ) {
            points[i] = this.edgeList.get(i).getP1();
        }

        return points;
    }

    /**
     * Check if this GeoGraph is a Valid GeoGraph:
     *  (1) No redundant input points
     *  (2) Internal Geometry is fully connected
     */
    public boolean isGeoGraph() {

        // Iterate through the edge list, ensure that each
        // edge represents a connected list
        GeoEdge start;
        for ( int i = 0; i < this.edgeList.size()-1; i++ ) {

            if ( i == this.edgeList.size()-1 ) {
                if ( !this.edgeList.get(1).getP1().equals(this.edgeList.get(i).getP2())) return false;
            } else {
                if ( !this.edgeList.get(i).getP2().equals(this.edgeList.get(i+1).getP2())) return false;
            }
        }

        return true; ///passed!
    }

    /**
     * Determine if two GeoGraphs are equal
     */
    @Override
    public boolean equals(Object other) {
        return equals(this, other);
    }

    /**
     * Two GeoGraphs are equal if all containing geoedges in the edge lists are equal
     * and in equal order
     */
    public static boolean equals( GeoGraph thiz, Object o ) {
        assert thiz != null;
        if ( thiz == o ) return true;
        if (!(o instanceof GeoGraph)) return false;

        GeoGraph g = (GeoGraph) o;

        // Iterate through edges and compare
        if ( g.getGeoEdges().size() != thiz.getGeoEdges().size() ) return false;

        for ( int i = 0; i < thiz.getGeoEdges().size(); i++ ) {
            if (!g.getGeoEdges().get(i).equals(thiz.getGeoEdges().get(i))) return false;
        }

        return true;
    }

    /**
     * Get the HashCode for this GeoGraph
     */
    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * All implementations of GeoGraph should use this definition of hashCode
     */
    public int hashCode( GeoGraph thiz ) {
        int result;
        long temp = 0;

        for ( int i = 0; i < thiz.getGeoEdges().size(); i++ ) {
            temp =+ thiz.getGeoEdges().get(i).hashCode();
        }

        result = (int) (temp ^ (temp >>> 32 ));
        return result;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        String edgeString = "GeoGraph\n";
        for ( int i = 0; i < this.edgeList.size(); i++ ) {
            edgeString = edgeString + this.edgeList.get(i).toString() + "\n";
        }
        return edgeString;
    }
}
