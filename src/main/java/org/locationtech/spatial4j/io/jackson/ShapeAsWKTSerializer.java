/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import java.io.StringWriter;

import org.locationtech.spatial4j.shape.Shape;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ShapeAsWKTSerializer extends JsonSerializer<Shape>
{
  @Override
  public void serialize(Shape value, JsonGenerator gen,
      SerializerProvider serializers) throws IOException,
      JsonProcessingException {
    
    StringWriter str = new StringWriter();
    value.getContext().getFormats().getWktWriter().write(str, value);
    gen.writeString(str.toString());
  }
}