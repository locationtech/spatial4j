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

package com.spatial4j.core.shape;

/**
 * A rectangle aligned with the axis (i.e. it is not at an angle).
 * <p/>
 * In geospatial contexts, it may cross the international date line (-180
 * longitude) if {@link #getCrossesDateLine()} however it cannot pass the poles
 * although it may span the globe.  It spans the globe if the X coordinate
 * (Longitude) goes from -180 to 180 as seen from {@link #getMinX()} and {@link
 * #getMaxX()}.
 */
public interface Rectangle extends Shape {

  /**
   * Expert: Resets the state of this shape given the arguments. This is a
   * performance feature to avoid excessive Shape object allocation as well as
   * some argument error checking. Mutable shapes is error-prone so use with
   * care.
   */
  public void reset(double minX, double maxX, double minY, double maxY);

  /**
   * The width. In geospatial contexts, this is generally in degrees longitude
   * and is aware of the international dateline.  It will always be >= 0.
   */
  public double getWidth();

  /**
   * The height. In geospatial contexts, this is in degrees latitude. It will
   * always be >= 0.
   */
  public double getHeight();

  /** The left edge of the X coordinate. */
  public double getMinX();

  /** The bottom edge of the Y coordinate. */
  public double getMinY();

  /** The right edge of the X coordinate. */
  public double getMaxX();

  /** The top edge of the Y coordinate. */
  public double getMaxY();

  /** Only meaningful for geospatial contexts. */
  public boolean getCrossesDateLine();

  /**
   * A specialization of {@link Shape#relate(Shape)}
   * for a vertical line.
   */
  public SpatialRelation relateYRange(double minY, double maxY);

  /**
   * A specialization of {@link Shape#relate(Shape)}
   * for a horizontal line.
   */
  public SpatialRelation relateXRange(double minX, double maxX);
}
