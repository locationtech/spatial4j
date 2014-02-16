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
import com.spatial4j.core.shape.impl.GeocentricPoint;
import com.spatial4j.core.shape.Vector3D;

import com.spatial4j.core.math.VectorUtils;
import com.spatial4j.core.distance.DistanceUtils;

/**
 * Compute the intersection point between two geodesics on the surface of a
 * sphere. Will compute a point and antipodal point, but always return the point
 * that is on the line which is the shorter haversin distance between the two points
 * Algorithm uses direciton cosines
 *
 * Algorithm from: http://www.movable-type.co.uk/scripts/latlong.htm
 *
 * Last Modified: 2/16/14
 */
public class GeodesicIntersection {

    /**
     * Just for while I am writing the code, keeping the algorithm description over here
     * comes straight from my write up on the blog
     *
     * For each point in latitude/longitude, where latitude - phi and longitude is lambda, we can define a unit vector u{x, y, z} = < cos(phi)cos(lambda, cos(phi)sin(lambda), sin(phi) >
     Compute the unit vector for each point p where p1, p2 is on arc 1, and p3, p4 is on arc2
     We then project the great circle path from our two points:
     Unit vector N - normal to the plane of great circle and x is the cross product
     N(u1, u2) = (u1 x u2)/||u1xu2||
     N(u3, u4) = (u3 x u4)/||u3xu4||
     Compute the intersection vector:
     N( N(u1, u2), N(u3, u4) )
     Return Lat/Lon result:
     First Point (phi, lambda): phi = atan2( uz, sqrt(ux^2, uy^2) )), lambda = atan2(uy, ux)
     Second point (antipodal): (-phi, lambda + pi)
     */

    private GeodesicIntersection() {}

    /**
     * Compute the Intersection between two geodesics defined by 3D geocentric points
     */
    public GeocentricPoint computeIntersection( Point p1, Point p2, Point p3, Point p4 ) {
       return compute( p1, p2, p3, p4 );
    }

    /**
     * Main Compute Method
     *
     * question I had - what if they don't intersect? I think in the case of
     * geodesics, becasue we project the arcs onto great circles and
     * great cirlces always have two intersection points that are valid, than this is
     * the case (evne if the intersection points are poles. THis is because
     * we are working on a spherical surface and not a traiditonal euclidean
     * plane
     *
     * i.e. beware do not stick regular points in here... not sure what would happen.
     */
    private GeocentricPoint compute( Point p1, Point p2, Point p3, Point p4 ) {

        // Compute the unit vector from lat/lon to xyz
        Vector3D u1 = computeUnitVector( p1.getX(), p1.getY() );
        Vector3D u2 = computeUnitVector( p2.getX(), p2.getY() );
        Vector3D u3 = computeUnitVector( p3.getX(), p3.getY() );
        Vector3D u4 = computeUnitVector( p4.getX(), p4.getY() );

        // Compute the normal vector for each set of points
        Vector3D Nu1u2 = computeNormal( u1, u2 );
        Vector3D Nu3u4 = computeNormal( u3, u4 );

        Vector3D r = computeNormal( Nu1u2, Nu3u4 );

        return new GeocentricPoint( r.getX(), r.getY(), r.getZ() );
    }

    /**
     * To unit vector
     * phi = latitude
     * lamdba = longitude
     */
    private Vector3D computeUnitVector( double phi, double lambda ) {
       double x = Math.cos(phi)*Math.cos(lambda);
       double y = Math.cos(phi)*Math.sin(lambda);
       double z = Math.sin(phi);

       return new Vector3D( x, y, z );
    }

    /**
     * Compute normal vector
     */
    private Vector3D computeNormal( Vector3D v1, Vector3D v2 ) {

        // Compute cross prod and magnitude of the cross product
        Vector3D crossProd = VectorUtils.crossProduct(v1, v2);
        double mag = VectorUtils.mag(crossProd);

        // Compute crossProd/mag(crossProd)
        Vector3D result = VectorUtils.multiply(crossProd, 1/mag);
        return result;
    }
}
