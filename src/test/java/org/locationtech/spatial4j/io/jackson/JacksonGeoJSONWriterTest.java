/*******************************************************************************
 * Copyright (c) 2017 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeneralGeoJSONTest;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

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
    // some anonymous impl that doesn't do anything
    Shape shape = new Shape() {
      @Override
      public SpatialRelation relate(Shape other) {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public Rectangle getBoundingBox() {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public boolean hasArea() {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public double getArea(SpatialContext ctx) {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public Point getCenter() {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public Shape getBuffered(double distance, SpatialContext ctx) {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }

      @Override
      public SpatialContext getContext() {
        return SpatialContext.GEO;
      }
    };

    String str = writer.toString(shape);
    Assert.assertTrue(str.indexOf("wkt")>0);
  }
}
