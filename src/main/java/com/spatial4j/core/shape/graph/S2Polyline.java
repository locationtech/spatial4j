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
import java.util.Set;
import java.util.HashSet;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.*;

import com.spatial4j.core.math.VectorUtils;

// hmmm...search utilities for things in a list of vertices that would be globally useful??

/// TODO Rebecca:
/// TODO: Implement crosser object
/// TODO: implement missing vector utilities
/// TODO: Implement s2 style transformation utilities
/// TODO: needs to rethink some of the access modifiers in this class - particularly those that might
/// TODO: dpbelong in a generic search methods class??

/**
 * There are some additional methods in this class that have not yet been implemented due to some
 * possible organizational changes to support the large number of methods about to be added here!!
 */

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
     * Get the list of vertices representing the polyline
     */
    public List< Vector3D > getVertices() {
        return this.vertices;
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
    public Vector3D getCentroid() {
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
        }
        return false;
    }

    /**
     * Returns a subsequence of vertex indices such that the polyline connecting
     * these indices is never further than the tolerance from the original polyline.
     * The first and last vertices are always preserved.
     *
     * Algorithm Notes:
     *  - O(n) runtime
     *  - Output is always a valid polyline
     *  - Non-Optimal, 2-5% fewer vertices than the Douglas-Peucker algorithm
     *    with similar tolerance
     *  - *Paramentrically* equivalent to the original polyline to
     *    within the given tolerance.
     */
    public void SubsampleVertices( double tolerance, List< Integer > indices ) {

        indices.clear();
        if ( numVertices() == 0 ) return;

        int counter = 0;
        indices.add(counter, 0);
        counter++;

        double clamped_tolerance = Math.max(tolerance, DistanceUtils.toRadians(0));
        for( int index = 0; index + 1 < vertices.size(); ) {
            int next_index = findEndVertex(*this, clamped_tolerance, index); // implement findEndVertex
            // Don't create duplicate adjacent vertices
            if (vertices.get(next_index) != vertices.get(index)) {
                indices.add(counter, next_index);
                counter++;
            }
            index = next_index;
        }
    }

    /**
     * Return true if two polylines have the same number of vertices and corresponding
     * vertex pairs are separated by no more than max_error
     *
     * Max error defualt setting seems to be 1e-15
     */
    public boolean approxEquals( S2Polyline line, double max_erorr ) {
        if (vertices.get(i) != line.numVertices()) return false;
        for (int offset = 0; offset < vertices.size(); offset++) {
            if (!VectorUtils.appeoxEquals(vertices.get(offset), line.getVertices().get(offset), max_error)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the first i > index such that the ith vertex of the polyline
     * is not at the same point as the index'th vertex. Returns the polyline.numVertices
     * if there is no such value of i
     */
    private int nextDistinctVertex( S2Polyline pline, int index ) {
        Vector3D initial = pline.getVertices().get(index);
        while ( index < pline.numVertices() && pline.getVertices().get(index).equals(initial) ) {
            index++;
        }
        return index;
    }

    /**
     * Search State Data structure required for nearly covers polyline method
     */
    private class SearchState {

        // data
        private final int i;
        private final int j;
        private final boolean i_in_progress;

        // Constructor
        public SearchState( int i_val, int j_val, boolean i_in_progress_val ) {
            i = i_val;
            j = j_val;
            i_in_progress = i_in_progress_val;
        }

        // Search State Comparator
        public boolean compareLess( SearchState lhs, SearchState rhs ) {
            if ( lhs.i < rhs.i ) return true;
            if ( lhs.i > rhs.i ) return false;
            if ( lhs.j < rhs.j ) return true;
            if ( lhs.j > rhs.j ) return false;
            return !lhs.i_in_progress && rhs.i_in_progress;

        }

        // Equals Method (required for contains comparator used below)
        @Override
        public boolean equals(Object o) {
            assert o != null;
            if ( this == o ) return true;
            if (!(o instanceof SearchState)) return false;

            SearchState s = (SearchState) o;

            if ( this.i != s.i ) return false;
            if ( this.j != s.j ) return false;
            if ( this.i_in_progress != s.i_in_progress ) return false;

            return true;
        }
    }

    /**
     * Returns true if the line is covered within max error of a contiguous subpath of this
     * polyine over its entire length. Specifically, returns true if this polyline has parameterization
     * a:[0,1]->s^2, "covered has parameterization b:[0,1]->s^2 and there is a non decreasing function
     * f:[0,1]->[0,1] such that the distance (a(f(t)), b(t)) <= max_error for all t.
     *
     * Note: This algorithm is described assuming adjacent vertices in
     * a polyline are never at the same point in space. Implementation
     * does not make this assumption.
     */
    public boolean nearlyCoversPolyline( S2Polyline covered, double max_error ) {

        // Useful Definitions:
        //  -edge "i" of a polyline is the edge from the ith to the i+1th vertex
        //  -covered_j is a polyline consisting of the edges 0 through j of covered
        //  -this_i is a polyline consisting of edges 0 through i of this polyline
        //
        // A search state is a tuple of (int, int bool)
        // DEFINITIONS:
        //   - edge "i" of a polyline is the edge from the ith to i+1th vertex.
        //   - covered_j is a polyline consisting of edges 0 through j of "covered."
        //   - this_i is a polyline consisting of edges 0 through i of this polyline.
        //   - A search state is represented as an (int, int, bool) tuple, (i, j,

        // TODO might want to replace these with java stacks for utility?
        List< SearchState > pending = new ArrayList<SearchState>();
        Set< SearchState > done = new HashSet<SearchState>();

        // Find all possible starting states
        int counter = 0;
        for (int i = 0, next_i = nextDistinctVertex(this, 0); next_i < this.numVertices();
             i = next_i, next_i = nextDistinctVertex(this, next_i)) {

            Vector3D closestPoint = SearchUtils.getClosestPoint( covered.vertices.get(0),
                    vertices.get(i), vertices.get(next_i));
            if ( closestPoint.equals(vertices.get(next_i)) &&
                    VectorUtils.angle(closestPoint, covered.getVertices().get(i)) <= max_error ) {
                pending.add(counter, SearchState(i, 0, true));
                counter++;
            }
        }

        int countDown = pending.size()-1;
        while (pending.size() != 0) {
            SearchState state = pending.get(countDown);
            pending.remove(countDown);
            countDown--;

            if ( !done.contains(state) ) continue;

            int next_i = nextDistinctVertex(this, state.i);
            int next_j = nextDistinctVertex(this, state.j);

            if (next_j == covered.numVertices()) return true;
            else if ( next_i == this.numVertices()) continue;

            Vector3D i_begin = new Vector3D(0, 0, 0);
            Vector3D j_begin = new Vector3D(0, 0, 0);

            if (state.i_in_progress) {
                i_begin = covered.getVertices().get(state.j);
                j_begin = SearchUtils.getClosestPoint(
                        j_begin, this.vertices.get(state.i), this.vertices.get(next-i));
            } else {
                i_begin = this.vertices(state.i);
                j_begin = SearchUtils.getClosestPoint(
                        i_begin, covered.getVertices().get(state.j), covered.getVertices().get(next_j));
            }

            // Will require either edge utils or search utils
            if ( SearchUtils.isEdgeNearA(j_begin, covered.getVertices().get(next_j),
                    i_begin, this.vertices(next_i), max_error)) {
                pending.add(counter, SearchState(next_i), state.j, false));
                counter++;
            }
            if ( SearchUtils.isEdgeNearA(i_begin, this.getVertices().get(next_i),
                    j_begin, covered.getVertices().get(next_j), max_error)) {
                pending.add(counter, SearchState(state.i, next_j, true));
            }
        }
        return false;
    }

    /// Some Additional Relational Methods //////////
    public boolean contains( Vector3D point ) {

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
