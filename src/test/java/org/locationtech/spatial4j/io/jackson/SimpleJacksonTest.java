/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.RandomizedShapeTest;
import org.locationtech.spatial4j.util.GeomBuilder;

import java.io.IOException;

public class SimpleJacksonTest extends RandomizedShapeTest {

  public SimpleJacksonTest() {
    super(JtsSpatialContext.GEO);
  }
  
  @Test
  public void testReadWrite() throws IOException {

    GeomBuilder builder = new GeomBuilder();
    
    ObjectWithGeometry obj = new ObjectWithGeometry();
    obj.name = "Hello";
    obj.shape = randomPointIn(ctx.getWorldBounds());
    obj.geo = null; //
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.registerModule(new ShapesAsGeoJSONModule());
//    mapper.registerModule(new ShapesAsWKTModule());
    
    String json = mapper.writeValueAsString(obj);
    
    System.out.println( json );
    
    ObjectWithGeometry out = mapper.readValue(json, ObjectWithGeometry.class);

    System.out.println( ">> AFTER <<" );
    System.out.println( mapper.writeValueAsString(out) );
  }
}
