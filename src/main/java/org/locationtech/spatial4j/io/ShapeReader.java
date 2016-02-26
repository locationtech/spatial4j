/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;

/**
 * Implementations are expected to be thread safe
 */
public interface ShapeReader extends ShapeIO {

  /**
   * @param value -- the input value, could be a String or other object
   * @return a shape valid shape (not null)
   */
  public Shape read(Object value) throws IOException, ParseException, InvalidShapeException;

  /**
   * @param value -- the input value, could be a String or other object
   * @return a shape or null, if the input was un readable.
   * 
   *         This will throw {@link InvalidShapeException} when we could read a shape, but it was
   *         invalid
   */
  public Shape readIfSupported(Object value) throws InvalidShapeException;

  /**
   * Read a {@link Shape} from the reader.
   * 
   * @param reader -- the input. Note, it will not be closed by this function
   * @return a valid Shape (never null)
   * @throws IOException
   * @throws ParseException
   * @throws InvalidShapeException
   */
  public Shape read(Reader reader) throws IOException, ParseException, InvalidShapeException;
}
