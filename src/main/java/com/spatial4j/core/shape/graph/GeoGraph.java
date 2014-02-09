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

/**
 * GeoGraph - Represents points in space connected by a geodesic distance
 *
 * shapes are immutable - so you cannot change the graph after it is constructed. You can cosntruct a new
 * one from scratch - but you cannot change its existing state.
 */
public class GeoGraph {

    // maybe store a spatial context?
    private GeoEdge[] edgeList;

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


    }

    /**
     * Is valid geograph
     * DAG invariants - mostly due to search invariants
     */

    /**
     * Return unique point set
     */

}