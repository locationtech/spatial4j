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
 * @file: GeocentricPoint.java
 * @brief: Interface to a Geocentric Point
 * @author: Rebecca Alford (ralford)
 *
 * @details Define a point in 3D Euclidean space (XYZ) that represents a point
 * on the spheroidal model of the earth. The Geocentric Point can also be described
 * as a vector from the center of the earth (0, 0, 0) to the defined XYZ geocentric point.
 * The geocentric point in Spatial4j alows shapes to be internally represented in 3D for polygonal
 * modeling
 *
 * @note Last Modified: 2/8/14
 */
/**
 * Interface: Direction Cosine Point
 * Define a point in 3D space that represents a point on the spheroidal
 * model of the earth by its direction cosines.
 *
 * Referneces http://www3.ul.ie/~mlc/support/Loughborough%20website/chap9/9_3.pdf
 * for computation of Direction Cosines
 */
public interface DirectionCosinePoint {

    /**
     * Reset the state of the direction cosine point given arguments. This feature
     * is a performance optimization to avoid excessive shape object allocation and
     * argument control.
     */
    public void reset( double a, double b, double g );

    /**
     * Set the X coordinate in the 3D geocentric point
     */
    public double getAlpha();

    /**
     * Get the Y coordinate in the 3D geocentric point
     */
    public double getBeta();

    /**
     * Get the Z coordinate in the 3D geocentric point
     */
    public double getGamma();

} // interface DirectionCosinePoint