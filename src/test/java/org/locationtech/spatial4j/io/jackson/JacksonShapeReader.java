/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.shape.Shape;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is really just a utility for testing
 */
public class JacksonShapeReader implements ShapeReader {

  final ObjectMapper mapper;
  
  public JacksonShapeReader(ObjectMapper m) {
    this.mapper = m;
  }
  
  @Override
  public String getFormatName() {
    return getClass().getSimpleName();
  }

  @Override
  public Shape read(Object value) throws IOException, ParseException, InvalidShapeException {
    String str = value.toString();
    return mapper.readValue(str, Shape.class);
  }

  @Override
  public Shape readIfSupported(Object value) throws InvalidShapeException {
    try {
      return read(value);
    } 
    catch (IOException | ParseException e) {
      return null;
    }
  }

  @Override
  public Shape read(Reader reader) throws IOException, ParseException, InvalidShapeException {
    return mapper.readValue(reader, Shape.class);
  }
}
