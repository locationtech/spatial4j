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

package com.spatial4j.core.shape;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.math.TransformUtils;
import com.spatial4j.core.shape.graph.Loop;

import javax.xml.crypto.dsig.Transform;
import java.util.List;
import java.util.ArrayList;


/**
 * Geodesic Polygon: A Geodesic Polyon is a composition of loops, which represent
 * implicit closed geometries on the surface of a sphere. Each loop is
 * assigned a depth with respect to the outer boundary of the polygon and
 * is specified at construction.
 *
 * A Geodesic polygon is represented internally in direction vectors to simplify
 * computation. Methods for converting between geographic points and direction
 * vectors are provided in core.math.TransformUtils.
 */
public class GeoPolygon implements Shape {

    // Store a list of loops in the polygon which maintain their
    // own depth.
    private List< Loop > loops;
    private SpatialContext ctx;
    private boolean isSimple;

    /**
     * Construct an Empty GeoPolygon
     */
    private GeoPolygon() {}

    /**
     * Construct a simple geodesic polygon (no holes) from a list
     * of lat/lng points
     */
    public GeoPolygon( List< Point > points ) {

        // GeoPolygon always in a geodesic context
        this.ctx = SpatialContext.GEO;

        // Make a list of vectors
        List< Vector3D > vertices = new ArrayList<Vector3D>();
        for ( int i = 0; i < points.size(); i++ ) {
            Vector3D v = TransformUtils.toVector(points.get(i));
            vertices.add(i, v);
        }

        // Construct a single loop of depth = 0
        Loop simpleLoop = new Loop( vertices, 0 , true );
        this.loops = new ArrayList< Loop >(1);
        this.loops.add(1, simpleLoop);

        // Set is_simple == true
        this.isSimple = true;

        // Assert polygon is valid
        assert( isValid() );
    }

    /**
     * Construct a complex polygon from a user specified list of
     * loops
     */
    public GeoPolygon( List< Loop > loops, boolean isSimple ) {
        this.ctx = SpatialContext.GEO;
        this.loops = loops;
        this.isSimple = isSimple;
        assert( isValid() );
    }

    /**
     * Check that our polygon is valid by checking overlap invariants
     * and valid loops
     */
    public boolean isValid() {
        return false; // not yet implemented
    }

    /**
     * Determine if we have a simple polygon (depth = 0, no holes)
     */
    public boolean isSimple() {
        return this.isSimple;
    }

    /**
     * Describe the Relationship between a polygon and another shape - determining
     * within, contains, disjoint and intersection. If the shapes are equal, then the result
     * contains or within will be returned.
     */
    @Override
    public SpatialRelation relate(Shape other) {
        throw new UnsupportedOperationException(); // TODO Implement this
    }

    /**
     * Compute the bounding box for the geodesic polygon. This means the
     * shape is within the bounding box and that it touches each side of the
     * rectangle
     */
    @Override
    public Rectangle getBoundingBox() {
        throw new UnsupportedOperationException(); // TODO IMPLEMENT THIS
    }

    /**
     * Does the Shape have area?
     */
    @Override
    public boolean hasArea() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Calculate the area of the shape in square-degrees. If no spatial
     * context is provided, then simple Euclidean calculations would be used
     * This figure can be an estimate.
     */
    public double getArea(SpatialContext ctx) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the center point of the polygon. Typically the same as the centroid of the bounding box.
     */
    @Override
    public Point getCenter() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a buffered version of a polygon. Buffer is usually a rounded corner buffer - though some
     * shapes might buffer differently.
     */
    @Override
    public Shape getBuffered(double distance, SpatialContext ctx ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


    /**
     * Shapes can be 'empty' if underlying coordinates are NaN
     */
    @Override
    public boolean isEmpty() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Implement Equals Method (polygons equal)
     */
    @Override
    public boolean equals(Object other) {
        return false; // TODO implement this
    }


    /**
     * Implement HashCode method (unique hascode for the shape)
     */
    @Override
    public int hashCode() {
        return 0; // TODO implement this
    }

    /**
     * To String - string representation of a polygon
     */
    @Override
    public String toString() {
        return "";
    }

    /**
     * Show method
     */
    public void show() {
        // print a string representation and other information about the current
        // shape - useful for debugging
    }

    /**
     * Private Method - Point in Polygon Algorithm
     */
    private void pointInPolygion() {

    }

    /**
     * Pairwise Lat/Lon Method for Computing the Bounding Box
     * void so it compiles for now
     */
    private void latLonBounding() {


        // Grab the outermost ring of the polygon
        Loop outerLoop = this.loops.get(1);

        // Keep track of the greatest distance between the two points

        for ( int i = 0; i < outerLoop.numVertices(); i++ ) {
            for ( int j = 1; j < outerLoop.numVertices(); j++ ) {

            }
        }



        // grab the outermost ring of the polygon

        // for all combinations of vertices, find the greatest
        // latitude span

        // for all combinations of vertices, find the greatest
        // longitude span

        // return a latitude/longitude bounding box from these bounds

        // formal way of checking validity of teh bod

    }

    /**
     * S2 Method for Computing the bounding box
     */

}
