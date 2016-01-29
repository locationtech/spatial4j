/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import java.io.IOException;
import java.io.Writer;
import org.locationtech.spatial4j.shape.Shape;

/**
 * Implementations are expected to be thread safe
 */
public interface ShapeWriter extends ShapeIO {

  /**
   * Write a shape to the output writer
   */
  public void write(Writer output, Shape shape) throws IOException;

  /**
   * Write a shape to String
   */
  public String toString(Shape shape);
}
