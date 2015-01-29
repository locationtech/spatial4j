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

package com.spatial4j.core.crs;

import com.spatial4j.core.shape.Point;

/**
 * Coordinate utility methods within a defined spherical coordinate reference system
 * (currently WGS84 lat/lon).  An instance of this class is created through the
 * {@link com.spatial4j.core.context.SpatialContextFactory}
 * todo expanded to accommodate Proj4J CoordinateReferenceSystem
 */
public interface CRSDelegate {
  public double normalizeX(double x);
  public double normalizeY(double y);
  public Point normalizePoint(Point p);
}
