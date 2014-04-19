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
import com.spatial4j.core.math.IntersectUtils;
import com.spatial4j.core.math.TransformUtils;
import com.spatial4j.core.shape.impl.PointImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A loop is a representation of a simple polygon on the surface of a sphere. Vertices
 * are represented in latitude and longitude, with an implicit closure between the first and
 * last vertex in the ring
 *
 * A loop has:
 *      (1) At least 3 vertices
 *      (2) No duplicate non-adjacent vertices
 *      (3) Non-adjacent edges cannot intersect
 *
 * This implementation of loops is inspired both by the jTS LinearRing and S2Loop implementations
 * for modeling general polygons.
 *
 * Link: https://code.google.com/p/s2-geometry-library/
 */
public class GeoLoop implements Shape {

    // Data: Store Loop Vertices
    private List< Point > vertices;
    private int depth;
    private boolean is_hole;
    private SpatialContext ctx;

    // Private Constructor - Must always construct a loop from vertices
    private Loop() {}

    /**
     * Construct a geodesic loop from a list of latitude/longitude points
     */
    public Loop(List<Point> vertices, int depth, boolean is_hole) {
        this.ctx = SpatialContext.GEO;
        this.vertices = vertices;
        this.depth = depth;
        this.is_hole = is_hole;
        assert( isValid() );
    }

    /**
     * Determine if this loop is a valid loop. Should always return true after
     * loop construction. Asserts the following invariants:
     *
     * A loop has:
     *      (1) At least 3 vertices
     *      (3) No duplicate non-adjacent vertices
     *      (4) Non-adjacent edges cannot intersect
     */
    public boolean isValid() {

        // Check num_vertices > 3;
        if ( vertices.size() < 3 ) {
            return false;
        }

        // Assert loops do not contain any duplicate non-adjacent vertices
        Map< Point, Integer > hashMap = new HashMap< Point, Integer >();
        for (int i = 0; i < vertices.size(); i++ ) {

            if ( !hashMap.containsKey(vertices.get(i))) {
                hashMap.put( vertices.get(i), i );
            } else if (i != vertices.size() && !vertices.get(i).equals(vertices.get(i+1))) {
                return false;
            }
        }

        // Assert Non-Adjacent edges are not allowed to intersect
        boolean crosses = false;

        // Iterate through vertices, predict intersection for each vertex
        int numEdges = numVertices()-1;
        for ( int i = 0; i < numEdges; i++ ) {

            // If I am not at the last edge..
            if ( i+2 < numVertices() ) {

                // Compute Possible Intersections between Adjacent Edges
                Vector3D a = TransformUtils.toVector(vertices.get(i));
                Vector3D b = TransformUtils.toVector(vertices.get(i+1));
                Vector3D c = TransformUtils.toVector(vertices.get(i+2));

                // Check edge intersection
                if ( IntersectUtils.edgeOrVertexIntersection(a, b, b, c) && !IntersectUtils.vertexIntersection(a, b, b, c) ) {
                    return false;
                }

            } else {

                // Compute Possible Intersections between Adjacent Edges
                Vector3D a = TransformUtils.toVector(vertices.get(i));
                Vector3D b = TransformUtils.toVector(vertices.get(i+1));
                Vector3D c = TransformUtils.toVector(vertices.get(1));

                // Check edge intersection
                if ( IntersectUtils.edgeOrVertexIntersection(a, b, b, c)  && !IntersectUtils.vertexIntersection(a, b, b, c) ) {
                    return false;
                }

            }
        }

       return true;
    }

    /**
     * Return the vertices currently contained in the loop
     */
    public List< Point > getVertices() {
        return this.vertices;
    }

    /**
     * Return the canonical first vertex of the loop
     */
    public Point getCanonicalFirstVertex() {
        assert( isValid() );
        return this.vertices.get(1);
    }

    /**
     * Get the depth of the loop within a polygon structure
     */
    public int depth() {
        return this.depth;
    }

    /**
     * Return number of vertices in the loop
     */
    public int numVertices() {
        return this.vertices.size();
    }

    /**
     * Is the loop a hole in the polygon?
     */
    public boolean isHole() {
        return is_hole;
    }

    /**
     * Find a vertex of interest in the loop
     */
    public Point findVertex( Point v ) {

        for ( int i = 0; i < this.vertices.size(); i++ ) {
            if (this.vertices.get(i).equals(v)) return this.vertices.get(i);
        }
        return new PointImpl(0, 0, ctx);
    }

    ////// Compute Geometric Properties of the Loop ///////

    /**
     * Compute the area of the loop
     */
    public double getArea() {
        throw new UnsupportedOperationException("Get Area not yet implemented");
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
     * Compute the lat/lon bounding box of this loop
     * O(n^2) search for the longest latitude span
     *
     * can seriously make this better, but just doing it this way for now
     */
    public Rectangle latLonBoundingBox() {

        double longest_lat = 0;
        int leftLat = 1;
        int rightLat = 1;

        // Find the longest latitude span
        for ( int i = 1; i <= vertices.size(); i++ ) {
            for ( int j = 1; j <= vertices.size(); j++ ) {

             // some utility that allows me to convert to a 2D vector and use the math there (would be pretty sweet!)
                double x1 = vertices.get(i).getX();
                double x2 = vertices.get(j).getX();

                double dist = Math.sqrt( Math.pow(x1, 2) + Math.pow(x2, 2) );

                if ( dist < longest_lat ) {
                    leftLat = i;
                    rightLat = j;
                    longest_lat = dist;
                }
            }
        }


        double longest_lon = 0;
        int topLon = 1;
        int bottomLon = 1;
        // Find the longest latitude span
        for ( int i = 1; i <= vertices.size(); i++ ) {
            for ( int j = 1; j <= vertices.size(); j++ ) {

                // some utility that allows me to convert to a 2D vector and use the math there (would be pretty sweet!)
                double x1 = vertices.get(i).getX();
                double x2 = vertices.get(j).getX();

                double dist = Math.sqrt( Math.pow(x1, 2) + Math.pow(x2, 2) );

                if ( dist < longest_lat ) {
                    leftLat = i;
                    rightLat = j;
                    longest_lat = dist;
                }
            }

        }

    }


    //// Useful Java Methods /////

    /**
     * All Loops maintain this .equals definition
     */
    @Override
    public boolean equals( Object o ) {
        return equals( this, o );
    }

    /**
     * Determine loop equality
     */
    public boolean equals( Loop thiz, Object other ) {
        assert thiz != null;
        if (thiz == other) return true;
        if (!(other instanceof Loop)) return false;

        Loop l = (Loop) other;

        if ( l.numVertices() != thiz.numVertices() ) return false;
        if ( l.isHole() != thiz.isHole() ) return false;
        if ( l.depth() != thiz.depth() ) return false;

        for ( int i = 0; i < l.numVertices(); i++ ) {
            if (! l.getVertices().get(i).equals(thiz.getVertices().get(i))) return false;
        }

        return true;
    }
}