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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing initialization and invariants in a a valid and
 * invalid GeoGraph
 */
public class GeoGraphTest {

    // Some possible test cases
    GeoGraph emptyGraph;
    GeoGraph singleEdgeGraph;
    GeoGraph twoEdgeGraph;
    GeoGraph threeEdgeGraph;
    GeoGraph largeGraph;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    /**
     * Testing Empty Graph Case
     */
    @Test
    public void testEmptyGraph() {

    }

    /**
     * Testing Single Edge Graph
     */
    @Test
    public void testSingleEdgeGraph() {

    }

    /**
     * Testing Two Edge Graph (cyclic edge case with inverses)
     */
    @Test
    public void testDoubleEdgeGraph() {

    }

    /**
     * Testing Three Edge Graph (simpelst non edge case graph)
     */
    @Test
    public void testTripleEdgeGraph() {

    }

    /**
     * Testing a Larger Graph
     */
    @Test
    public void testLargeGraph() {

    }

    /**
     * Testing A Graph containing a redundant point list
     */
    @Test
    public void testRedundantPoints() {

    }

    /**
     * Testing Graph Traversal (will eventually reach
     * the last point in the graph after traversing n nodes)
     */
    private boolean canTraverse( GeoGraph g ) {
        return false;

    }

    /**
     * Testing non redundant points list is equal to the
     * number of edges in the graph
     */
    private boolean checkNREdges( GeoGraph g ) {
        return false;

    }

    /**
     * Testing for a given graph with n non redundant points,
     * there are n-1 edges
     */
    private boolean checkEdgeNums( GeoGraph g ) {
        return false;
    }



}
