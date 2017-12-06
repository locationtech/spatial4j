/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeneralGeoJSONTest;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.locationtech.spatial4j.shape.Shape;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
/**
 * This test compares the jackson JSONWriter to the standard GeoJSON Writer
 */
public class JacksonGeoJSONWriterTest extends GeneralGeoJSONTest {

  @Before
  @Override
  public void setUp() {
    super.setUp();
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ShapesAsGeoJSONModule());
    JacksonShapeWriter w = new JacksonShapeWriter(mapper);
    
    reader = ctx.getFormats().getReader(ShapeIO.GeoJSON);
    writer = w;
    writerForTests = writer;

    Assert.assertNotNull(reader);
    Assert.assertNotNull(writer);
    Assert.assertNotNull(writerForTests);
  }
  

  @Test
  public void testWriteUnknownAsWKT() throws Exception {
    Shape shape = mock(Shape.class);
    when(shape.getContext()).thenReturn(SpatialContext.GEO);
    
    String str = writer.toString(shape);
    Assert.assertTrue(str.indexOf("wkt")>0);
  }
}
