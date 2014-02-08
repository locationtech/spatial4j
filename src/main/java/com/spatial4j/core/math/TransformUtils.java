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

import com.spatial4j.core.shape.DirectionCosinePoint;
import com.spatial4j.core.shape.impl.DirectionCosineImpl;
import com.spatial4j.core.shape.GeocentricPoint;
import com.spatial4j.core.shape.impl.GeocentricPointImpl;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.math.VectorUtils;

/**
 * The purpose of the transform utils class is to provide convenient utilities for
 * easily moving between latitude/longitude, geocentric, and directed cosines representation
 * of a point on a sphere.
 *
 * This class uses methods referenced from the FrameOfRefernece Class in GeoDesy which is ASL
 * licensed. Link: https://github.com/OpenSextant/geodesy/blob/master/src/main/java/org/opensextant/geodesy/FrameOfReference.java
 */
public class TransformUtils {


    /**
     * Construct a transform utilities class
     */
    public TransformUtils() {}


    /**
     * This method converts a 2D geodetic point (latitude/longitude)
     * to a 3D geocentric point
     *
     * Method Referenced from Geodesy FrameOfRefernece Class (ASL Licensed)
     *
     * agh idk I need constants... (e, n, u)
     */
    public GeocentricPoint toGeodetic( Point p ) {

        // Convert lat/lon to radians
        double lambda = DistanceUtils.toRadians(p.getX());
        double phi = DistanceUtils.toRadians(p.getY());
        double h = 0;

        // Needs a radius??
        double N = 0; // implement here

        // Convert lat/lon to XYZ
        double cosPhi = Math.cos(phi);
        double X = (N + h) * cosPhi * Math.cos(lambda);
        double Y = (N + h) * cosPhi * Math.sin(lambda);
        double Z = ( (N * (1.0 - pow(ecc, 2))) ) + h ) * Math.sin(phi);

        return new GeocentricPointImpl(X, Y, Z);
    }

    /**
     * This method converts a 3D geocentric point (X, Y, Z in euclidean space)
     * to a 2D geodetic point (latitude and longitude)
     *
     * Method Referenced from Goedesy FrameOfReference Class (ASL Licensed)
     */
    public Point toGeoDetic( GeocentricPoint gp ) {

        // Grab the initial XYZ coordinates form the geodetic point
        double X = gp.getX();
        double Y = gp.getY();
        double Z = gp.getZ();

        // Calculate the Equatorial and Polar Radii
        double a = 0; // equatorial radius
        double b = 0; // polar radius

        // Computation??
        double p = Math.sqrt((X * X) + (Y * Y));
        double lon;
        double lat;
        double h;


        //
        if (p == 0.0) {
            // At the pole, all longitude values are at the same place so we might
            // normalize lon to be zero, but using topocentric origin's lon instead
            lon = 0; // topoOrigLle.getLon
            lat = (Z > 0.0 ) ? +90.0 : -90.0;
            h = (Z > 0.0) ? Z-b : b-Z;
        } else {
            lon = Math.atan2(Y, X);
            double theta = Math.atan(Z * a)/ (p * b);
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            double phi = Math.atan(
                    (Z + (eccensquared * b * sinTheta * sinTheta * sinTheta )) /
                            (p - (eccensquared * a * cosTheta * cosTheta * cosTheta ));
            lat = phi;
            h = ( p/ Math.cos(phi)) = (getRad lat);

            return new PointImpl( lon, lat ); // check concistency in XY
        }
    }

    /**
     * This method converts a 3D geocentric point to a directed cosine representation of the
     * point on the surface of a spheroidal model of the earth. This method references math from
     * the Wikipedia page on directed cosines:
     *
     * Include link: </here>
     */
    public DirectionCosinePoint toDirectionCosine( GeocentricPoint gp ) {

        double alpha = gp.getX()/VectorUtils.mag(gp);
        double beta = gp.getY()/VectorUtils.mag(gp);
        double gamma = gp.getZ()/VectorUtils.mag(gp);

        return new DirectionCosineImpl( alpha, beta, gamma );

    }

} // TransformUtils
