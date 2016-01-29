/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;
import org.junit.Test;

import java.io.*;
import java.text.ParseException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ShapeFormat}
 */
public class ShapeFormatTest {
  
  public Shape testReadAndWriteTheSame(Shape shape, ShapeReader reader,ShapeWriter writer) throws IOException, ParseException {
    assertNotNull(shape);
    
    StringWriter str = new StringWriter();
    writer.write(str, shape);
  //  System.out.println( "OUT: "+str.toString());
    
    Shape out = reader.read(new StringReader(str.toString()));
    
    StringWriter copy = new StringWriter();
    writer.write(copy, out);
    assertEquals(str.toString(), copy.toString());
    return out;
  }
  
  public void testCommon(SpatialContext ctx, String name) throws Exception {
    ShapeReader reader = ctx.getFormats().getReader(name);
    ShapeWriter writer = ctx.getFormats().getWriter(name);
    assertNotNull(reader);
    assertNotNull(writer);
    testReadAndWriteTheSame(ctx.makePoint(10, 20),reader,writer);
    testReadAndWriteTheSame(ctx.makeLineString(
        Arrays.asList(
            ctx.makePoint(1, 2),
            ctx.makePoint(3, 4),
            ctx.makePoint(5, 6)
        )),reader,writer);
    
   // testReadAndWriteTheSame(ctx.makeRectangle(10, 20, 30, 40),format);
  }

  public void testJTS(JtsSpatialContext ctx, String name) throws Exception {
    ShapeReader reader = ctx.getFormats().getReader(name);
    ShapeWriter writer = ctx.getFormats().getWriter(name);
    Shape shape;
    
//    
//    wkt = readFirstLineFromRsrc("/russia.wkt.txt");
//    shape = ctx.readShape(wkt);
//  //  testReadAndWriteTheSame(shape,format);
    
    // Examples from Wikipedia
    shape = wkt(ctx,"LINESTRING (30 10, 10 30, 40 40)");
  //  testReadAndWriteTheSame(shape,format);

    shape = wkt(ctx,"POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10))");
    testReadAndWriteTheSame(shape,reader,writer);
    
    shape = wkt(ctx,"POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
    testReadAndWriteTheSame(shape,reader,writer);

    shape = wkt(ctx,"MULTIPOINT ((10 40), (40 30), (20 20), (30 10))");
    testReadAndWriteTheSame(shape,reader,writer);

    shape = wkt(ctx,"MULTIPOINT (10 40, 40 30, 20 20, 30 10)");
    testReadAndWriteTheSame(shape,reader,writer);
    
    shape = wkt(ctx,"MULTILINESTRING ((10 10, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
    testReadAndWriteTheSame(shape,reader,writer);

    shape = wkt(ctx,"MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
    testReadAndWriteTheSame(shape,reader,writer);
  }

  @Test
  public void testReadAndWriteTheSame() throws Exception {
    // GeoJSON
    String format = ShapeIO.GeoJSON;
    testCommon(SpatialContext.GEO, format);
    testCommon(JtsSpatialContext.GEO, format);
    testJTS(JtsSpatialContext.GEO, format);
    
    // WKT
    format = ShapeIO.WKT;
    testCommon(SpatialContext.GEO, format);
    testCommon(JtsSpatialContext.GEO, format);
    testJTS(JtsSpatialContext.GEO, format);
  }
  
  public void testParseVsInvalidExceptions(ShapeReader reader, boolean supportsPolygon) throws Exception {
    String txt = null;
    try {
      txt = "garbage";
      reader.read(txt);
      fail("should throw invalid exception");
    } catch(ParseException ex) { 
      //expected
    }
    
    try {
      txt = "POINT(-1000 1000)";
      reader.read(txt);
      fail("should throw invalid shape");
    } catch(InvalidShapeException ex) { 
      //expected
    }
    
    if(supportsPolygon) {
      try {
        txt = readFirstLineFromRsrc("/fiji.wkt.txt");
        reader.read(txt);
        fail("should throw invalid exception");
      } catch(InvalidShapeException ex) { 
        //expected
      }
    }
  }

  @Test
  public void testParseVsInvalidExceptions() throws Exception {
    testParseVsInvalidExceptions(SpatialContext.GEO.getFormats().getWktReader(), false);
    testParseVsInvalidExceptions(JtsSpatialContext.GEO.getFormats().getWktReader(), true);
  }
  

  private String readFirstLineFromRsrc(String wktRsrcPath) throws IOException {
    InputStream is = getClass().getResourceAsStream(wktRsrcPath);
    assertNotNull(is);
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      return br.readLine();
    } finally {
      is.close();
    }
  }

  /** Convenience to read static data. */
  protected Shape wkt(SpatialContext ctx, String wkt) throws IOException, ParseException {
    return ctx.getFormats().getWktReader().read(wkt);
  }
}
