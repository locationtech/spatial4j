/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Ryan McKinley - initial API and implementation
 ******************************************************************************/

package com.spatial4j.core.io.jts;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.WKTWriter;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;

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
