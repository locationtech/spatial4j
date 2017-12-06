/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;

import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.shape.Shape;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.locationtech.jts.geom.Geometry;

public class GeometryDeserializer extends JsonDeserializer<Geometry>
{
  // Create a context that will allow any JTS shape
  static final JtsSpatialContext JTS;
  static {
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.geo = false;
    factory.useJtsLineString = true;
    factory.useJtsMulti = true;
    factory.useJtsPoint = true;
    JTS = new JtsSpatialContext(factory);
  }
  
  final ShapeDeserializer dser;
  
  public GeometryDeserializer() {
    dser = new ShapeDeserializer(JTS);
  }
  
  @Override
  public Geometry deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Shape shape = dser.deserialize(jp, ctxt);
    if(shape!=null) {
      return JTS.getShapeFactory().getGeometryFrom(shape);
    }
    return null;
  }
}
