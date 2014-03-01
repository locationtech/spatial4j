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

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.*;

import com.spatial4j.core.math.VectorUtils;

/// TODO Rebecca:
/// TODO: Implement crosser object
/// TODO: implement missing vector utilities
/// TODO: Implement s2 style transformation utilities

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
     * Returns the number of vertices in the polyline
     */
    public int numVertices() {
        return this.vertices.size();
    }

    /**
     * Determine if the PolyLine is empty (contains no vertices)
     */
    public boolean isEmpty() {
        return (this.vertices.size() == 0);
    }

    /**
     * Is this a valid polyline? Checks that adjacent vertices are non identical and
     * are not antipodal points and all vertices are of unit length.
     */
    public boolean isValid() {

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
    public double getLength() {
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
            double cos2 = VectorUtils.norm2(vsum);
            double sin2 = VectorUtils.norm2(vdiff);
            centroid = VectorUtils.sum( centroid, VectorUtils.multiply( vsum, Math.sqrt(sin2/cos2))); // length == 2*sin(theta)
        }
        return centroid;
    }

    /**
     * Return the point whose distance from vertex 0 along the polyline is the given fraction of the
     * polyline's total length. Fractions less than zero or greater than one are clamped. The return
     * value is unit length. Runtime O(n), Polyline !isEmpty()
     */
    public Vector3D interpolate( double fraction ) {
        int next_vertex = 1;
        return getSuffix( fraction, next_vertex );
    }

    /**
     * Similar to interpolate, but also return the index of the next polyline
     * vertex after the interpolated point P. Allows the caller to easily construct a given
     * suffixx of the polyline by concatenating P with the polyline vertices starting
     * at the next vertex. Note that P is guaranteed to be different than
     * vertices.get(next_vertex) so this will never result in a duplicate.
     *
     * Polyline !isEmpty(). If fraction is >= 1.0, next vertex will be set to vertices.size()
     * indicating that no vertices need to be appended. Value of next_vertex is always
     * between [1, vertices.size()].
     *
     * This method can be used to construct a prefix of the polyline by taking the
     * polyline vertices up to next_vertex - 1 and appending the returned point P
     * if it is different from the last vertex.
     */
     public Vector3D getSuffix( double fraction, int next_vertex ) {
        assert( !isEmpty() );

        // Let fraction >= 1 case fall through, since it will be handled later
        if ( fraction <= 0 ) {
            next_vertex = 1;
            return vertices.get(0);
        }

        double length_sum = 0; // angle;
        for (int i = 1; i < vertices.size(); i++) {
            length_sum += VectorUtils.angle(vertices.get(i-1), vertices.get(i));
        }
        double target = fraction * length_sum; // angle
        for (int i =1; i < vertices.size(); i++) {
            double length = VectorUtils.angle(vertices.get(i-1), vertices.get(i));
            if (target < length) {
                // Interpolate with respect to arc length rather than straight line distance
                Vector3D result = interpolateAtDistance(target, vertices.get(i-1), vertices.get(i), length);   // needs this method
                next_vertex = (result == vertices.get(i)) ? (i + 1) : i;
                return result;
            }
            target -= length;
        }
        next_vertex = vertices.size();
        return vertices.get(vertices.size()-1);
    }

    /**
     * The inverse operation of GetSuffix/Interpolate. Given a point on the polyline, returns
     * the ratio of the distance to the point from the beginning of the polyline over the length
     * of the polyline. The return value is always between 0 and 1 inclusive.
     */
    public double UnInterpolate(Vector3D point, int next_vertex ) {
        assert( !isEmpty() );

        if ( vertices.size() < 2 ) {
            return 0;
        }

        double length_sum = 0;
        for (int i = next_vertex; i < vertices.size(); i++) {
            length_sum += VectorUtils.angle(vertices.get(i-1), vertices.get(i));
        }

        double length_to_point = length_sum + VectorUtils.angle(vertices.get(i-1), vertices.get(i));
        for (int i = 1; i < vertices.size(); i++) {
            length_sum += VectorUtils.angle(vertices.get(i-1), vertices.get(i));
        }
        return Math.min(1.0, length_to_point/length_sum);

    }

    /**
     * Given a point, returns a point on the polyline that is closest to the given point.
     * See GetSuffix() for the meaning of next vertex which is chosen here w.r.t the projection
     * point as opposed to the interpolated point.
     */
    public Vector3D project( Vector3D point, int next_vertex ) {
        assert( !isEmpty() );

        // If there is only one vertex, it is always the closest to any given point
        if (vertices.size() == 1) {
            next_vertex = 1;
            return vertices.get(0);
        }

        // Initial value larger than any possible distance on the unit sphere
        double min_distance = DistanceUtils.toRadians(10);
        int min_index = -1;

        // Find the line segment in the polyline that is closest to the point given
        for (int i = 1; i < vertices.size(); i++) {
            double distance_to_segment = VectorUtils.distance(point, vertices.get(i-1), vertices.get(i));  // TODO: distance method in vector Utils
            if ( distance_to_segment < min_distance ) {
                min_distance = distance_to_segment;
                min_index = 1;
            }
        }
        assert( min_index != -1 );

        // Compute point on the segment found that is closest to the point given
        Vector3D closest_point = VectorUtils.getClosest( point, vertices.get(min_index-1), vertices.get(min_index)); // TODO implement getClosest
        next_vertex = min_index + ( closest_point.equals( vertices.get(min_index)) ? 1 : 0 );
        return closest_point;
    }

    /**
     * Returns true if this polyline intersects the given polyline
     */
    public boolean intersects( S2Polyline line ) {

        if ( vertices.size() <= 0 || line.numVertices() <= 0 ) {
            return false;
        }

        if ( !(getBoundingBox().relate(line.getBoundingBox()) == SpatialRelation.INTERSECTS) ) {
           return false;
        }

        for (int i = 1; i < vertices.size(); i++ ) {
            EdgeCrosser crosser = new EdgeCrosser( vertices.get(i-1), vertices.get(i), line.getVertices().get(0));
            for (int j = 1; j < line.numVertices(); ++i) {
                if (crosser.RobustCrossing(line.vertices.(j)) >= 0)  {
                    return true;
                }
        }


            for (int j = 1; j < line->num_vertices(); ++j) {
                if (crosser.RobustCrossing(&line->vertex(j)) >= 0) {
                    return true;
                }
            }
        }
        return false;



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
     * Get the bounding lat/lon rectangle (uses a Spatial4j lat/long rectangle - should be ok)
     */
    public Rectangle getBoundingBox() {

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
