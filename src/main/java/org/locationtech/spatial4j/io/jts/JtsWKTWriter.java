/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jts;

import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.io.WKTWriter;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

/**
 * Writes the WKT using JTS directly
 */
public class JtsWKTWriter extends WKTWriter {

  public JtsWKTWriter(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {

  }

  @Override
  public String toString(Shape shape) {
    if (shape instanceof JtsGeometry) {
      return ((JtsGeometry) shape).getGeom().toText();
    }
    return super.toString(shape);
  }
}
