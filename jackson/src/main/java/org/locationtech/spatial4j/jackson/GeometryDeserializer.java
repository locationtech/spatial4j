package org.locationtech.spatial4j.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryDeserializer extends JsonDeserializer<Geometry>
{
  @Override
  public Geometry deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    
    JsonNode node = jp.getCodec().readTree(jp);
    String txt = node.asText(null);
    if(txt==null) {
      return null;
    }
    try {
      return new WKTReader().read(txt);
    } catch (ParseException e) {
      throw new JsonParseException("error reading geometry", jp.getCurrentLocation(), e);
    }
  }
}
