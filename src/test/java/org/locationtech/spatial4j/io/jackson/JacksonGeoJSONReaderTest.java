/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.io.GeneralGeoJSONTest;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.locationtech.spatial4j.shape.Shape;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test compares the jackson JSONWriter to the standard GeoJSON Writer
 */
public class JacksonGeoJSONReaderTest extends GeneralGeoJSONTest {

  @Before
  @Override
  public void setUp() {
    super.setUp();
    ctx = JtsSpatialContext.GEO;
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ShapesAsGeoJSONModule());
    
    reader = new JacksonShapeReader(mapper);
    writer = ctx.getFormats().getWriter(ShapeIO.GeoJSON);
    writerForTests = writer; 
    
    Assert.assertNotNull(reader);
    Assert.assertNotNull(writer);
    Assert.assertNotNull(writerForTests);
  }
  
  @Override
  @Test
  public void testEncodeBufferedLineString() throws Exception {
    // the JTS buffered LineString becomes a polygon!
    Shape out = reader.read( bufferedLineText() );
    
    assertEquals(out.getClass(), bufferedLine().getClass());
  }
  
  

}
