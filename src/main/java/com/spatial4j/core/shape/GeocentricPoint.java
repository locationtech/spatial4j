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
 * Interface: Geocentric Point
 * Define a point in 3D space that represents a point on the spheroidal
 * model of the earth. The geocentric point in spatial 4j allows shapes to be internally
 * represented in 3D geocentric coordinates for polygonal modeling.
 */
public interface GeocentricPoint {

    /**
     * Reset the state of the geocentric point given arguments. This feature
     * is a performance optimization to avoid excessive shape object allocation and
     * argument control.
     */
    public void reset( double x, double y, double z );

    /**
     * Set the X coordinate in the 3D geocentric point
     */
    public double getX();

    /**
     * Get the Y coordinate in the 3D geocentric point
     */
    public double getY();

    /**
     * Get the Z coordinate in the 3D geocentric point
     */
    public double getZ();

} // interface GeocentricPoint