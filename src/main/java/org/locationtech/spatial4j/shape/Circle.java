/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

/**
 * A circle, also known as a point-radius since that is what it is comprised of.
 */
public interface Circle extends Shape {

  /**
   * Expert: Resets the state of this shape given the arguments. This is a
   * performance feature to avoid excessive Shape object allocation as well as
   * some argument error checking. Mutable shapes is error-prone so use with
   * care.
   */
  void reset(double x, double y, double radiusDEG);

  /**
   * The distance from the point's center to its edge, measured in the same
   * units as x &amp; y (e.g. degrees if WGS84).
   */
  double getRadius();

}
