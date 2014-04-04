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
 * Last Modified: 2/23/14
 */
public class TransformUtils {

    private TransformUtils() {}

    /**
     * Convert a lat/long point to its equivalent unit length vector
     * Method taken from s2LatLng toPoint
     * Link: https://code.google.com/p/s2-geometry-library/source/browse/geometry/s2latlng.h
     */
    public static Vector3D toVector( Point p ) {

        double phi = DistanceUtils.toRadians(p.getY());
        double theta = DistanceUtils.toRadians(p.getX());

        double cosPhi = Math.cos(phi);
        return new Vector3D( Math.cos(theta)*cosPhi, Math.sin(theta)*cosPhi, Math.sin(phi) );
    }

    /**
     * Convert a direction vector to its equivalent lat/lng point (not necessarily unit length
     * Method taken from S2LatLng explicit conversion from Point to Lat/Lng obj
     * Link: https://code.google.com/p/s2-geometry-library/source/browse/geometry/s2latlng.h
     */
    public static Point toPoint( Vector3D v, SpatialContext ctx ) {

        double lat = Math.atan2( v.getZ(), Math.sqrt( Math.pow( v.getX(), 2 )+ Math.pow( v.getY(), 2 ) ));
        double lon = Math.atan2( v.getY(), v.getX() );

        return new PointImpl( lon, lat, ctx );
    }

} // TransformUtils
