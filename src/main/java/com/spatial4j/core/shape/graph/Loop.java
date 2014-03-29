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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.spatial4j.core.math.Vector3DUtils;
import com.spatial4j.core.math.CCW;

import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.Rectangle;

/**
 * A loop is a representation of a simple polygon on the surface of a sphere. Vertices
 * are represented as 3D direction cosine vectors (derived from the 3D geocentric point)
 * and are listed counter clockwise with an implicit closure between the last and
 * first vertex on the ring.
 *
 * A loop has:
 *      (1) At least 3 vertices
 *      (2) All vertices of unit length
 *      (3) No duplicate vertices
 *      (4) Non-adjacent edges cannot intersect
 *
 * Various loop modeling algorithms are modeled after the s2Loop implementation in the
 * s2Geometry project which is under Apache (ASL) license. More info on this project
 * can be found at:
 *
 * Link: https://code.google.com/p/s2-geometry-library/
 */
public class Loop {

    // Data: Store Loop Vertices
    private List< Vector3D > vertices;
    private int depth;
    private boolean is_hole;

    private Loop() {}

    /**
     * Construct a geodesic loop from a list of vertices (3D Point)
     */
    public Loop( List< Vector3D > vertices, int depth, boolean is_hole ) {
        this.vertices = vertices;
        this.depth = depth;
        this.is_hole = is_hole;
        assert( isValid() );
    }

    ////// Methods for Loop Properties ///////

    /**
     * Determine if this loop is a valid loop. Should always return true after
     * loop construction. Asserts the following invariants:
     *
     * A loop has:
     *      (1) At least 3 vertices
     *      (2) All vertices of unit length
     *      (3) No duplicate vertices
     *      (4) Non-adjacent edges cannot intersect
     */
    public boolean isValid() {

        // Check num_vertices > 3;
        if ( vertices.size() < 3 ) {
            return false;
        }

        // Check all vertices are of unit length
        for ( int i = 0; i < vertices.size(); i++ ) {
            if ( Vector3DUtils.mag(vertices.get(i)) != 1 ) {
                return false;
            }
        }

        // Assert loops do not contain any duplicate vertices
        Map< Vector3D, Integer > hashMap = new HashMap< Vector3D, Integer >();
        for (int i = 0; i < vertices.size(); i++ ) {
            if ( !hashMap.containsKey(vertices.get(i))) {
                hashMap.put( vertices.get(i), i );
            } else {
                return false;
            }
        }

        // Assert Non-Adjacent edges are not allowed to intersect
        boolean crosses = false;

        // Iterate through vertices, predict intersection for each vertex
        for ( int i = 0; i < vertices.size(); i++ ) {
            // still needs to implement
        }

       return true;
    }

    /**
     * Return the vertices currently contained in the loop
     */
    public List< Vector3D > getVertices() {
        return this.vertices;
    }

    /**
     * Return the cannonical first vertex of the loop
     */
    public Vector3D getCanonicalFirstVertex() {
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
    public Vector3D findVertex( Vector3D v ) {

        for ( int i = 0; i < this.vertices.size(); i++ ) {
            if (this.vertices.get(i).equals(v)) return this.vertices.get(i);
        }
        return new Vector3D(0, 0, 0); // otherwise, return a non-unit vector
    }

    ////// Compute Geometric Properties of the Loop ///////

    /**
     * Does the loop have area?
     */
    public boolean hasArea() {
        return (getArea() > 0 );
    }

    /**
     * Compute the area of the loop
     */
    public double getArea() {
        throw new UnsupportedOperationException("Get Area not yet implemented");
    }

    /**
     * Compute the centroid of the loop
     */
    public Vector3D getCenter() {
        throw new UnsupportedOperationException("Get centroid not yet implemented");
    }

    /**
     * Get the Bounding Lat/Lon Rectangle of the Loop
     */
    public Rectangle getBoundingBox() {
        throw new UnsupportedOperationException("Get bounding bod not yet implemented");
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
