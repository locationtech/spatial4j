package org.locationtech.spatial4j.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

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
    
    JsonNode node = jp.getCodec().readTree(jp);
    String txt = node.asText(null);
    if(txt==null) {
      return null;
    }
    try {
      return ctx.getFormats().read(txt);
    } catch (Exception e) {
      throw new JsonParseException("error reading shape", jp.getCurrentLocation(), e);
    }
  }
}
