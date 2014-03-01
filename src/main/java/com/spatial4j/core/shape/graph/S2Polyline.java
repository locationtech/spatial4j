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

import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.Point;

import com.spatial4j.core.math.VectorUtils;

/**
 * S2 Implementation of a Geodesic Line defined by a series of points
 * Used in Spatial4j for computing intersections between geodesics
 *
 * Represents a sequence of zero or more vertices connected by straight
 * edges (geodesics)       \
 *
 * Fast utility in 3D - easier to not convert between for performance reasons
 */
public class S2Polyline  {

    // Data
    private List< Vector3D > vertices;

    /**
     * Construct an Empty S2 Polyline
     */
    public S2Polyline() {
        this.vertices = new ArrayList< Vector3D >();
    }

    /**
     * Initialize a Polyline that connects the list of given vertices. Empty polylines
     * are allowed. Adjacent vertices should not be identical or antipodal.
     */
    public S2Polyline( List< Vector3D > vertices ) {
        this.vertices = vertices;
        assert( isValid() );
    }

    /**
     * Determine if the PolyLine is empty (contains no vertices)
     */
    boolean isEmpty() {
        return (this.vertices.size() == 0);
    }

    /**
     * Is this a valid polyline? Checks that adjacent vertices are non identical and
     * are not antipodal points and all vertices are of unit length.
     */
    boolean isValid() {

        // All vertices are unit length
        for ( int i = 0; i < vertices.size(); i++ ) {
            if ( VectorUtils.mag(vertices.get(i)) != 1 ) {
                return false;
            }
        }

        // Adjacent vertices must not be identical or antipodal
        for (int i = 1; i < vertices.size(); i++ ) {
            if ( vertices.get(i-1).equals(vertices.get(i)) ||
                    vertices.get(i-1).equals(VectorUtils.multiply(vertices.get(i), -1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return the length of the polyline
     */
    double getLength() {
        double length = 0;
        for ( int i = 1; i < vertices.size(); i++ ) {
            length += VectorUtils.angle( vertices.get(i-1), vertices.get(i));
        }
        return length;
    }

    /**
     * Return the true centroid of the polyline multiplied by the length of the polyline.
     * The result is not unit length but can be normalized. Prescaling length makes it
     * easy to compute the centroid of multiple polylines.
     */
    Vector3D getCentroid() {
        Vector3D centroid = new Vector3D(0, 0, 0);
        for (int i = 1; i < vertices.size(); i++ ) {
            Vector3D vsum = VectorUtils.sum( vertices.get(i-1), vertices.get(i));
            Vector3D vdiff = VectorUtils.difference( vertices.get(i-1), vertices.get(i));
            double
        }
    }


        double cos2 = vsum.Norm2();
        double sin2 = vdiff.Norm2();
        DCHECK_GT(cos2, 0);  // Otherwise edge is undefined, and result is NaN.
        centroid += sqrt(sin2 / cos2) * vsum;  // Length == 2*sin(theta)
    }
    return centroid;

    /**
     * Return the poitn whose distance from vertex 0 is the given fraciton of the polyline's total
     * length. Fractions less than 0 or greater than 1 are clamped. Return value is unit lenght.
     * Polyine must not be empty
     */
    Vector3D interpolate( double fraction ) {

    }


    /**
     * Return the index of the next polyline after the interpolatio of the point p. Allows
     * caller to easily construct a given suffix of the polyline by concatenating p
     * with the polyline.
     * P is guaranteed to be different than vertex next so will never get duplicates.
     *
     * Polyline must not be empty.
     *
     * some more description....
     */
    Vector3D getSuffix( double fraction, Vector3D next_vertex ) {

    }

    /**
     * Inverse operation of getSuffix/interpolate. given a point on the polyline, return
     * the ratio of the distance to the point of the beginning of the polyline over
     * the length of the polyline. Return value x in [0, 1]
     */
    double UnInterpolate(Vector3D point, int next_vertex ) {

    }

    /**
     * Given a point, returns a point on the polyline that is closest to the given point.
     * See GetSuffix() for the meaning of next vertex which is chosen here w.r.t the projection
     * point as opposed to the interpolated point.
     */
    Vector3D project( Vector3D point, int next_vertex ) {

    }

    /**
     * Returns true if this polyline intersects the given polyline
     */
    boolean intersects( S2Polyline line ) {

    }

    /**
     * Reverse the order of vertices listed currently in the s2 polyline
     */
    public void reverse() {}

    /**
     * Return a subsequence of vertex indices such that
     * the polyline connecting these indices is never further than the tolerance
     * from the original polyline. The first and last vertices are always
     * preserved.
     *
     *
     * straihgt from their code
     *   // Some useful properties of the algorithm:
     //
     //  - It runs in linear time.
     //
     //  - The output is always a valid polyline.  In particular, adjacent
     //    output vertices are never identical or antipodal.
     //
     //  - The method is not optimal, but it tends to produce 2-3% fewer
     //    vertices than the Douglas-Peucker algorithm with the same tolerance.
     //
     //  - The output is *parametrically* equivalent to the original polyline to
     //    within the given tolerance.  For example, if a polyline backtracks on
     //    itself and then proceeds onwards, the backtracking will be preserved
     //    (to within the given tolerance).  This is different than the
     //    Douglas-Peucker algorithm used in maps/util/geoutil-inl.h, which only
     //    guarantees geometric equivalence.
     */
    void SubsampleVertices( double tolerance, List< Integer > indices ) {

    }

    /**
     * Return true if two polylines have the same number of vertices and corresponding
     * vertex pairs are separated by no more than max_error
     *
     * Max error defualt setting seems to be 1e-15
     */
    boolean approxEquals( S2Polyline line, double max_erorr ) {

    }

    /**
     * Return true if "covered" is within "max_error of a contiguous subpath of
     * this polyline over its entire length. Specifically, returns true if this polyline has parameterization a:[0,1]
     * -> s^2, "covered" has parameterization b:[0,1]->S^2 and there is a non decreasing function f:[0, 1] -> [0,1]
     * such that the distance(a(f(t)), b(t)) <= max_error for all t.
     */
    boolean nearlyCoversPolyline( S2Polyline covered, double max_error ) {

    }

    /// Some Additional Relational Methods //////////
    boolean contains( Vector3D point ) {

    }

    /**
     * Exact java .equals method
     */
    @Override
    public boolean equals( Object o ) {
        return equals( this, o );
    }

    /**
     * Definiton of full equality for 2 s2 polylines
     */
    public boolean equals( S2Polyline thiz, Object o ) {

    }

    /**
     * HashCode for the polyline
     */
    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * Definition of hashCode for a polyline
     */
    public int hashCode( S2Polyline line ) {

    }

    /**
     * toString Method for polyline
     */
    @Override
    public String toString() {
        return toString(this);
    }

    /**
     * Definition of toString for polyline
     */
    public String toString( S2Polyline thiz ) {

    }

}
