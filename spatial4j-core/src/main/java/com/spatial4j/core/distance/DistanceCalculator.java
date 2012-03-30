/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.IPoint;
import com.spatial4j.core.shape.IRectangle;

public interface DistanceCalculator {

  public double distance(IPoint from, IPoint to);
  public double distance(IPoint from, double toX, double toY);

  public IPoint pointOnBearing(IPoint from, double dist, double bearingDEG, SpatialContext ctx);
  
  /**
   * Converts a distance to radians (multiples of the radius). A spherical
   * earth model is assumed for geospatial, and non-geospatial is the identity function.
   */
  public double distanceToDegrees(double distance);

  public double degreesToDistance(double degrees);

  //public Point pointOnBearing(Point from, double angle);

  public IRectangle calcBoxByDistFromPt(IPoint from, double distance, SpatialContext ctx);

  public double calcBoxByDistFromPtHorizAxis(IPoint from, double distance, SpatialContext ctx);

}
