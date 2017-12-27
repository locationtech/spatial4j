/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

/**
 * A rectangle aligned with the axis (i.e. it is not at an angle).
 * <p>
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
   * and is aware of the dateline (aka anti-meridian).  It will always be &gt;= 0.
   */
  public double getWidth();

  /**
   * The height. In geospatial contexts, this is in degrees latitude. It will
   * always be &gt;= 0.
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
