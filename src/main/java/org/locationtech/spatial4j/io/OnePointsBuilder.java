/*******************************************************************************
 * Copyright (c) 2016 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.ShapeFactory;

/** INTERNAL class used by some {@link ShapeReader}s. */
public class OnePointsBuilder implements ShapeFactory.PointsBuilder<OnePointsBuilder> {
  private ShapeFactory shapeFactory;
  private Point point;

  public OnePointsBuilder(ShapeFactory shapeFactory) {
    this.shapeFactory = shapeFactory;
  }

  @Override
  public OnePointsBuilder pointXY(double x, double y) {
    assert point == null;
    point = shapeFactory.pointXY(x, y);
    return this;
  }

  @Override
  public OnePointsBuilder pointXYZ(double x, double y, double z) {
    assert point == null;
    point = shapeFactory.pointXYZ(x, y, z);
    return this;
  }

  @Override
  public OnePointsBuilder pointLatLon(double latitude, double longitude) {
    assert point == null;
    point = shapeFactory.pointLatLon(latitude, longitude);
    return this;
  }

  public Point getPoint() {
    return point;
  }
}
