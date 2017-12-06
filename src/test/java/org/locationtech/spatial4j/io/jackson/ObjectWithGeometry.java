/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import org.locationtech.spatial4j.shape.Shape;

import org.locationtech.jts.geom.Geometry;

public class ObjectWithGeometry {
  public String name;
  public Geometry geo;
  public Shape shape;
}
