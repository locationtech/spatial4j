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

import com.spatial4j.core.shape.graph.GeoGraph;

/**
 * Project To Do List (Opened 2/9/14):
 *
 *
 */

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
    private
    private GeoGraph polygon;
    private Point[] points;
    private SpatialContext ctx;

    /**
     * Construct an Empty GeoPolygon
     */
    private GeoPolygon() {}

    /**
     * Construct a geodesic polygon from a list of ordered points
     */
    public GeoPolygon( Point[] points, SpatialContext ctx  ) {
        this.ctx = ctx;
        init( points );
    }

    /**
     * Construct a Geodesic Polygon from a list of points by
     * building a GeoGraph.
     */
    private void init( Point[] points ) {
        this.polygon = new GeoGraph( points );
        this.points = points;
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

}
