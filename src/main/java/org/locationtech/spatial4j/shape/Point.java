/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

/**
 * A Point with X &amp; Y coordinates.
 */
public interface Point extends Shape {

  /**
   * Expert: Resets the state of this shape given the arguments. This is a
   * performance feature to avoid excessive Shape object allocation as well as
   * some argument error checking. Mutable shapes is error-prone so use with
   * care.
   */
  public void reset(double x, double y);

  /** The X coordinate, or Longitude in geospatial contexts. */
  public double getX();

  /** The Y coordinate, or Latitude in geospatial contexts. */
  public double getY();

  /** Convenience method that usually maps on {@link org.locationtech.spatial4j.shape.Point#getY()} */
  public double getLat();

  /** Convenience method that usually maps on {@link org.locationtech.spatial4j.shape.Point#getX()} */
  public double getLon();

}
