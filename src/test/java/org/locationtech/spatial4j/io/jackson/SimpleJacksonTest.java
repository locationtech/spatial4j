/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.RandomizedShapeTest;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SimpleJacksonTest extends RandomizedShapeTest {

  public SimpleJacksonTest() {
    super(JtsSpatialContext.GEO);
  }

  @Test
  public void testReadWriteShapeAsGeoJSON() throws IOException {
    ObjectWithGeometry obj = new ObjectWithGeometry();
    obj.name = "Hello";
    obj.shape = ctx.getShapeFactory().pointXY(11,12); // Spatial4j type
    obj.geo = null; //

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.registerModule(new ShapesAsGeoJSONModule());

    String json = mapper.writeValueAsString(obj);

    ObjectWithGeometry out = mapper.readValue(json, ObjectWithGeometry.class);
    assertEquals(obj.shape, out.shape);
  }

  @Test
  public void testReadWriteJtsAsWKT() throws IOException {
    final JtsShapeFactory shapeFactory = ((JtsSpatialContext) ctx).getShapeFactory();

    ObjectWithGeometry obj = new ObjectWithGeometry();
    obj.name = "Hello";
    obj.shape = null;
    obj.geo = shapeFactory.getGeometryFactory().createPoint(new Coordinate(11, 12)); // JTS type

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new ShapesAsWKTModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    String json = objectMapper.writeValueAsString(obj);

    assertEquals("{\"name\":\"Hello\",\"geo\":\"POINT (11 12)\"}", json);

    ObjectWithGeometry deserialized = objectMapper.readValue(json, ObjectWithGeometry.class);
    assertEquals(obj.geo, deserialized.geo);
  }
}
