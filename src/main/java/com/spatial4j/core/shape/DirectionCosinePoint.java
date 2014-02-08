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

package com.spatial4j.core.shape;


/**
 * @file: DirectionCosinePoint.java
 * @brief: Interface to a Direction Cosine Point
 * @author: Rebecca Alford (ralford)
 *
 * @details Define a point in 3D Euclidean space defined by its XYZ direction cosines that represents
 * a point on a spheroidal model of the earth by its direciton cosines. Provides avenues to fast
 * compotations of geodesic intersections.
 *
 * Information for Implementation of Direction Cosine Points can be found from:
 * http://www3.ul.ie/~mlc/support/Loughborough%20website/chap9/9_3.pdf
 *
 * @note Last Modified: 2/8/14
 */
public interface DirectionCosinePoint {

    /**
     * @brief Reset the Direction Cosine Point
     * @details Reset the state of the direction cosine point given arguments. This feature
     * is a performance optimization to avoid excessive shape object allocation and
     * argument control.
     */
    public void reset( double a, double b, double g );

    /**
     * Get the Alpha component of the direction cosine point
     */
    public double getAlpha();

    /**
     * Get the Beta component of the direction cosine point
     */
    public double getBeta();

    /**
     * Get the Gamma compnent of the direction cosine point
     */
    public double getGamma();

} // interface DirectionCosinePoint