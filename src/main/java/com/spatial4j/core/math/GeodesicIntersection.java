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

import com.spatial4j.core.shape.impl.GeocentricPoint;

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

    private GeodesicIntersection() {}

    /**
     * Compute teh Intersection between two geodesics defined by 3D geocentric points
     */
    public GeocentricPoint computeIntersection( GeodesicIntersection p1, GeodesicIntersection p2 ) {

    }

    /**
     * Main Compute Method
     */
    private GeocentricPoint compute() {

    }

    /**
     * To unit vector
     */
    private
    /**
     * Compute normal vector
     */


}
