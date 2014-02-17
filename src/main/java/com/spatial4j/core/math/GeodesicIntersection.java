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

package com.spatial4j.core.math;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.GeocentricPoint;
import com.spatial4j.core.shape.Vector3D;

import com.spatial4j.core.math.VectorUtils;
import com.spatial4j.core.distance.DistanceUtils;

import com.spatial4j.core.context.SpatialContext;

/**
 * Compute the intersection point between two geodesics. This algorithm
 * takes two points and projects it onto the corresponding great circle. Two
 * great circles will always have 2 intersection points, however I then extend
 * this algorithm to determine if the intersection point lies within range of either
 * original geodesic to test the relevant point and non-intersection.
 *
 * This algorithm interfaces with geographic coordinates but works internally
 * with direction cosines.
 *
 * Algorithm from: http://www.movable-type.co.uk/scripts/latlong.htm
 *
 * Last Modified: 2/16/14
 */
public class GeodesicIntersection {

    private GeodesicIntersection() {}

    /**
     * Compute the Intersection between two geodesics defined by 3D geocentric points
     */
    public GeocentricPoint computeIntersection( Point p1, Point p2, Point p3, Point p4, SpatialContext ctx ) {
       //return intersection( p1, p2, p3, p4, ctx );
        return null;
    }

    /**
     * Compute the Two Intersection Points on the projected great circles
     * from the defined line segments
     *
     *      (1) Project Lat/Lon points onto a great circle and compute the
     *          unit vector describing each point.
     *      (2) Compute the normal vector between each pair of points (to represent
     *          the corresponding great circle)
     *      (3) Compute the normal vector between the two pairs.
     *      (4) Return the corresponding point Point[1] and Antipodal point Point[2[
     */
    public Point intersection( Point p1, Point p2, Point p3, Point p4, SpatialContext ctx ) {

        // Naming: Nu1u2 - Normal Vector to first geodesic, Nu3u4 - Normal Vector
        // to second geodesic

        // Compute the unit vector from lat/lon to xyz
        Vector3D u1 = computeUnitVector( p1.getX(), p1.getY() );
        Vector3D u2 = computeUnitVector( p2.getX(), p2.getY() );
        Vector3D u3 = computeUnitVector( p3.getX(), p3.getY() );
        Vector3D u4 = computeUnitVector( p4.getX(), p4.getY() );

        // Compute the normal vector for each set of points
        Vector3D Nu1u2 = computeNormal( u1, u2 );
        Vector3D Nu3u4 = computeNormal( u3, u4 );

        // Compute resultant normal r.
        Vector3D r = computeNormal( Nu1u2, Nu3u4 );

        // Extract Point and antipodal point
        Point[] points = getPointPair(r, ctx);

        // Determine the appropriate intersection point in range.
        // If no points intersect return out of bounds lat/lon??
        Point result = getPointInRange( points );

        return points;
    }
    /**
     * Compute the unit vector using direction cosines from 2D geographic
     * points. Phi = latitude and Lambda = longitude.
     */
    private Vector3D computeUnitVector( double phi, double lambda ) {
       double x = Math.cos(phi)*Math.cos(lambda);
       double y = Math.cos(phi)*Math.sin(lambda);
       double z = Math.sin(phi);

       return new Vector3D( x, y, z );
    }

    /**
     * Compute the normal vector from 2 given vectors
     */
    private Vector3D computeNormal( Vector3D v1, Vector3D v2 ) {

        // Compute cross prod and magnitude of the cross product. Return
        // the quotient of these two.
        Vector3D crossProd = VectorUtils.crossProduct(v1, v2);
        double mag = VectorUtils.mag(crossProd);
        return VectorUtils.multiply(crossProd, 1/mag);

    }

    /**
     * Get the point and antipodal point pair from a resulting unit
     * vector in direction cosine
     */
    private Point[] getPointPair( Vector3D r, SpatialContext ctx ) {

        // Construct a SpatialContext

        // Get point 1 from r
        double phi1 = Math.atan2( r.getZ(), Math.sqrt( Math.pow(r.getX(), 2) + Math.pow(r.getY(), 2)));
        double lambda1 = Math.atan2( r.getY(), r.getX() );
        Point p1 = new PointImpl( phi1, lambda1, ctx ); // hhmm...needs a spatial context.

        // Get antipodal point 2 from r
        double phi2 = -1*phi1;
        double lambda2 = lambda1 + Math.PI;
        Point p2 = new PointImpl( phi2, lambda2, ctx );

        // Create a list of points and return
        Point[] points = new Point[2];
        points[0] = p1;
        points[1] = p2;

        return points;
    }

    /**
     * From a pair of points computed as intersection points, determine
     * if these two points are in range of the line segment shortest
     * distance.
     */
    private Point getPointInRange( Point[] points ) {

        /**
         * Convert points to geocentric
         * find min/max points
         * determine if the test points are in range??
         */
        return null; // not yet implemented. TODO
    }
}

