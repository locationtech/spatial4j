/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ShapeDeserializer extends JsonDeserializer<Shape>
{
  final SpatialContext ctx;

  public ShapeDeserializer() {
    this(SpatialContext.GEO);
  }
  
  public ShapeDeserializer(SpatialContext ctx) {
    this.ctx = ctx;
  }
  
  @Override
  public Shape deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    
    ShapeFactory f = ctx.getShapeFactory();
    
    // Essentially reading as DOM
    JsonNode node = jp.readValueAsTree();
    if(node instanceof ObjectNode) {
      ObjectNode n = (ObjectNode)node;
      String type = n.get("type").asText();
      JsonNode coords = n.get("coordinates");
    }
    else if(node!=null){
      String txt = node.asText();
      if(txt!=null) {
        if(txt==null) {
          return null;
        }
        try {
          return ctx.getFormats().read(txt);
        } catch (Exception e) {
          throw new JsonParseException(jp, "error reading shape", e);
        }
      }
    }
    return null;
  }
}