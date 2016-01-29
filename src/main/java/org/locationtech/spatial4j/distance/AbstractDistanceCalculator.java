/*******************************************************************************
 * Copyright (c) 2015 MITRE and VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.distance;

import org.locationtech.spatial4j.shape.Point;

/**
 */
public abstract class AbstractDistanceCalculator implements DistanceCalculator {

  @Override
  public double distance(Point from, Point to) {
    return distance(from, to.getX(), to.getY());
  }

  @Override
  public boolean within(Point from, double toX, double toY, double distance) {
    return distance(from, toX, toY) <= distance;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
