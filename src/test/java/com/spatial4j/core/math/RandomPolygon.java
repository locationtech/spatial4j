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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.impl.PointImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This code was written to try generating some random polygons for testing. It never went anywhere
 * because this method only generates concave polygons which most likely represents <1% of the known
 * polygons, but could possibly be expanded in the future.
 */

/**
 * Module for generating random polygons in their Spatial4j internal representation - direction
 * cosine vectors. This class contains code for computing random vectors in both oriented and non
 * oriented conformations. It is not an extension of the randomized testing packages and just uses
 * Java Math.Random()
 *
 * Everything here is done with respect to a unit circle with origin (0, 0) and radius of 1, so its definition
 * in this class is implicit.
 */
public class RandomPolygon {

    // Required for backwards conversion
    private SpatialContext ctx;

    /**
     * Constructor for RandomPolygon
     */
    public RandomPolygon() {
        ctx = SpatialContext.GEO;
    }

    /**
     * Create a set of Random CCW Direction Cosine Vectors.
     */
    public List< Vector3D > createVectors( boolean ccw, int minBinSize, int maxBinSize ) {

        // Pick some n from [0, maxBin]
        int n = (int) (minBinSize + Math.random() * (maxBinSize - minBinSize));

        // Pick n random numbers from 0 to 360 and sort
        List< Double > bins = new ArrayList< Double >(n);
        for ( int i = 0; i < n; i++ ) {
            double num = Math.random() * 360;
            bins.add(num);
        }

        // Sort my list
        Collections.sort(bins);

        // Convert to radians and generate a set of vectors to return
        List< Vector3D > vectors = new ArrayList<Vector3D>(n);
        for ( int i = 0; i < bins.size(); i++ ) {
            double angle =  bins.get(i);
            Point p = new PointImpl( Math.cos(angle), Math.sin(angle), ctx);
            Vector3D v = TransformUtils.toVector(p);
            vectors.add(v);
        }

        return vectors;
    }


    /**
     * Make a Random Set of Oriented Points
     *
     * note - num points was unused so I removed the arg though it might be a good point of
     * extension
     */
    private List< Vector3D > make_random_point_set( boolean oriented, boolean ccw ) {


        /**
         * Note - the reason I am explicitly picking four points here and not just picking three points
         * on the circle is because if I have a point in each bin, I get some sort of reliable angle distribution
         * and can predict ordering of angles for testing.
         */

        // Pick four random points in the quadrants of the circle
        List<Point> pointArr = new ArrayList< Point >(4);

        // Quadrant 1
        double alpha = randomAngle(0.0, 90.0);
        Point a = new PointImpl( Math.cos(alpha), Math.sin(alpha), ctx);
        pointArr.add(a);

        // Quadrant 2
        double beta = randomAngle(90.0, 180.0);
        Point b = new PointImpl( Math.cos(beta), Math.sin(beta), ctx);
        pointArr.add(b);

        // Quadrant 3
        double gamma = randomAngle(180.0, 270.0);
        Point c = new PointImpl( Math.cos(gamma), Math.sin(gamma), ctx);
        pointArr.add(c);

        // Quadrant 4
        double chi = randomAngle(270.0, 360.0);
        Point d = new PointImpl( Math.cos(chi), Math.sin(chi), ctx);
        pointArr.add(d);

        // Establish a final array of points converted to direction cosines
        List< Vector3D > vectorArr = new ArrayList< Vector3D >(3);

        // Declare 3 indices
        int ind1, ind2, ind3;

        // If no orientation, pick three random indices
        if ( !oriented ) {

            // Pick the first 3 points
            List< Point > copy = new ArrayList<Point>(pointArr);
            Collections.shuffle(copy);

            for ( int i = 0; i < copy.size(); i++ ) {
                Vector3D v = TransformUtils.toVector(copy.get(i));
                vectorArr.add(v);
            }
            return vectorArr;

        } else if ( oriented && !ccw ) {

            // Pick a random leave out point
            int leaveOut = (int) Math.random() * 3; // should return int [0, 3] inclusive

            // Pick 3 of 4 points in clockwise orientation
            for ( int i = 0; i < pointArr.size(); i++ ) {
                if ( i != leaveOut) {
                    Vector3D v = TransformUtils.toVector(pointArr.get(i));
                    vectorArr.add(v);
                }
            }
            return vectorArr;

            // otherwise, oriented && ccw are true
        } else {

            // Pick a random leave out point
            int leaveOut = (int) Math.random() *3; // should return int [0, 3] inclusive

            // Pick 3 of 4 points in clockwise orientation
            for ( int i = pointArr.size()-1; i >= 0; i-- ) {
                if ( i != leaveOut) {
                    Vector3D v = TransformUtils.toVector(pointArr.get(i));
                    vectorArr.add(v);
                }
            }
            return vectorArr;
        }
    }

    /**
     * Compute a Random Angle within the range of angles given, inclusive [lower, upper]
     * Input is in degrees, output is in radians for convenience with other methods.
     */
    public double randomAngle( double lower, double upper ) {
        double angle = lower + (Math.random() * (upper - lower + 1));
        return DistanceUtils.toRadians(angle);
    }

}
