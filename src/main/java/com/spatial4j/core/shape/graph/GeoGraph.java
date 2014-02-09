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
 * the connection between 2 vertices and the geodesic distance between them.
 *
 * Because the shape of a GeoPolygon itself is immutable, a GeoGraph is also immutable once constructed
 * but can be reset from scratch if needed.
 */
public class GeoGraph {

    // Represent a graph internally as a list of edges
    private List< GeoEdge > edgeList;

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

        this.edgeList = new ArrayList<GeoEdge>(points.length -1); // n-1 edges in a bounding polygon for n points

        // For each point, create an edge between the two points
        for ( int i = 0; i < points.length; i++ ) {
            GeoEdge e = new GeoEdge( points[i], points[i+1] );
            this.edgeList.add(e);
        }

        // Connect the ends of the polygon
        GeoEdge connect = new GeoEdge( points[0], points[points.length-1] );

    }

    /**
     * Is Connected - Verify that the polygon is a closed shape
     */
    private void isConnected() {

    }

    /**
     * Is Valid GeoGraph. Some proposed Invariants:
     *  (1) No redundant input points (unique geometry)
     *  (2) internal geometry is fully connected.
     */

    /**
     * Is valid geograph
     * DAG invariants - mostly due to search invariants
     */

    /**
     * Return unique point set
     */

}
