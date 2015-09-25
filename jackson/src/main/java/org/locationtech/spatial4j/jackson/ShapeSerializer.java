package org.locationtech.spatial4j.jackson;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.spatial4j.core.shape.Shape;

public class ShapeSerializer extends JsonSerializer<Shape>
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
