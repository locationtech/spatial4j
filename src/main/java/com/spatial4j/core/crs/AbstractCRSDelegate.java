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
 * Base Coordinate Reference System Delegate for an unbounded Cartesian System
 */
public abstract class AbstractCRSDelegate implements CRSDelegate {
  // TODO will be replaced with Proj4J coordinate reference system
  protected double MIN_X = Double.MIN_VALUE;
  protected double MIN_Y = Double.MIN_VALUE;
  protected double MAX_X = Double.MAX_VALUE;
  protected double MAX_Y = Double.MAX_VALUE;
  protected double X_RANGE = Double.MAX_VALUE;
  protected double Y_RANGE = Double.MAX_VALUE;

  @Override
  public double normalizeX(double x) {
    return x;
  }

  @Override
  public double normalizeY(double y) {
    return y;
  }

  @Override
  public Point normalizePoint(Point p) {
    return p;
  }
}
