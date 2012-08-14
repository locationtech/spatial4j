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
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

/**
 * Performs calculations relating to distance, such as the distance between a
 * pair of points.  A calculator might be based on Euclidean space, or a
 * spherical model, or ellipsoid, or maybe even something else.
 */
public interface DistanceCalculator {

  /** The distance between <code>from</code> and <code>to</code>. */
  public double distance(Point from, Point to);

  /** The distance between <code>from</code> and <code>Point(toX,toY)</code>. */
  public double distance(Point from, double toX, double toY);

  /**
   * Calculates where a destination point is given an origin (<code>from</code>)
   * distance, and bearing (given in degrees -- 0-360).  If reuse is given, then
   * this method may reset() it and return it.
   */
  public Point pointOnBearing(Point from, double distDEG, double bearingDEG, SpatialContext ctx, Point reuse);

  /**
   * Calculates the bounding box of a circle, as specified by its center point
   * and distance.
   */
  public Rectangle calcBoxByDistFromPt(Point from, double distDEG, SpatialContext ctx, Rectangle reuse);

  /**
   * The <code>Y</code> coordinate of the horizontal axis (e.g. left-right line)
   * of a circle.  The horizontal axis of a circle passes through its furthest
   * left-most and right-most edges. On a 2D plane, this result is always
   * <code>from.getY()</code> but, perhaps surprisingly, on a sphere it is going
   * to be slightly different.
   */
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distDEG, SpatialContext ctx);

  public double area(Rectangle rect);

  public double area(Circle circle);

}
