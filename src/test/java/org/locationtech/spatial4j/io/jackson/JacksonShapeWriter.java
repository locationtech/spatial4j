package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import java.io.Writer;

import org.locationtech.spatial4j.io.ShapeWriter;
import org.locationtech.spatial4j.shape.Shape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is really just a utility for testing
 */
public class JacksonShapeWriter implements ShapeWriter {

  final ObjectMapper mapper;
  
  public JacksonShapeWriter(ObjectMapper m) {
    this.mapper = m;
  }
  
  @Override
  public String getFormatName() {
    return getClass().getSimpleName();
  }

  @Override
  public void write(Writer output, Shape shape) throws IOException {
    output.write(toString(shape));
  }

  @Override
  public String toString(Shape shape) {
    try {
      return mapper.writeValueAsString(shape);
    } 
    catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
