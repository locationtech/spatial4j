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

import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.impl.GeocentricPoint;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.context.SpatialContext;

/**
 * TransformUtils allows easily moving between 3 coordinate references: geographic coordinates
 * (traditional Spatial4j latitude/longitude), geocentric coordinates and direction cosine points.
 * These three systems are different representations of a point on the surface of a sphere.
 *
 * Computations for moving between GeoGraphic to GeoCentric Coordinates come from
 * http://kartoweb.itc.nl/geometrics/coordinate%20transformations/coordtrans.html
 * and were adapted from the ellipsoidal model to the spherical model.
 *
 * Last Modified: 2/22/14
 */
public class TransformUtils {

    private TransformUtils() {}

    /**
     * Convert a Geographic Point (latitude and longitude) to a Geocentric
     * Point (x, y, z) on the spherical model of the earth (h = 0)
     *
     * Note: if you are trying to follow the calculation directly from the reference
     * provided above, this is now the nomenclature works out
     *      x => longitude => lambda
     *      y => latitude => phi
     */
    public static GeocentricPoint toGeocentric( Point p ) {

        // Compute u (prime vertical radius of curvature and radius
        double r = DistanceUtils.EARTH_MEAN_RADIUS_KM;

        // Compute X, Y, and Z
        double X = r * Math.cos(p.getY()) * Math.cos(p.getX());
        double Y = r * Math.cos(p.getY()) * Math.sin(p.getY());
        double Z = r * Math.sin(p.getY());

        return new GeocentricPoint(X, Y, Z);
    }

    /**
     * Convert a Geocentric Point ((X, Y, Z) in Euclidean Space) to a Geographic
     * point (latitude and longitude)
     *
     * ** Approximates lon at the poles to be 0 (no topoOrigin available??)
     * Method Referenced from OpenSextant/Goedesy FrameOfReference Class (ASL Licensed)
     */
    public static Point toGeodetic( GeocentricPoint gp, SpatialContext ctx ) {

        // Initial Declarations to Clean up appearance of the algorithm
        double x = gp.getX();
        double y = gp.getY();
        double z = gp.getZ();

        double r = DistanceUtils.EARTH_MEAN_RADIUS_KM;

        // Variable declarations
        double lat;
        double lon;

        // Compute Phi
        double phi = Math.sqrt( Math.pow(gp.getX(), 2) + Math.pow(gp.getY(), 2));

        if ( phi == 0.0 ) {
            // At a pole, longitude values all superimpose so we will normalize to 0
            lon = 0;
            lat = z > 0.0 ? +90.0 : -90.0;
        } else {
            lon = Math.atan2(y, x);
            double theta = Math.atan((z * r)/(phi * r));
            double phi2 = Math.atan(z/phi);
            lat = phi2;
        }

        return new PointImpl(lon, lat, ctx);
    }

    /**
     * Convert a 3D Geocentric Point to a Direction Cosine Vector. Method referenced
     * from the Wikipedia page on direction cosines.
     *
     * http://en.wikipedia.org/wiki/Direction_cosine
     */
    public static Vector3D toDirectionCosine( GeocentricPoint gp ) {

       double a = gp.getX()/VectorUtils.mag(gp);
       double b = gp.getY()/VectorUtils.mag(gp);
       double g = gp.getZ()/VectorUtils.mag(gp);

       return new Vector3D( a, b, g );

    }

} // TransformUtils
