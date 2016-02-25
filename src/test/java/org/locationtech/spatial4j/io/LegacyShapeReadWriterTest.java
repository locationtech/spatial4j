/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.shape.Shape;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class LegacyShapeReadWriterTest extends RandomizedTest {

  private final LegacyShapeReader reader;
  private final LegacyShapeWriter writer;

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
            $(SpatialContext.GEO),
            $(JtsSpatialContext.GEO)
    ));
  }

  private final SpatialContext ctx;

  public LegacyShapeReadWriterTest(SpatialContext ctx) {
    this.ctx = ctx;
    this.reader = new LegacyShapeReader(ctx, null);
    this.writer = new LegacyShapeWriter(ctx, null);
  }

  @SuppressWarnings("unchecked")
  private <T extends Shape> T writeThenRead(T s ) throws IOException {
    String buff = writer.toString( s );
    return (T) read( buff );
  }
  
  private Shape read(String value) {
    return reader.readIfSupported(value);
  }
  
  @Test
  public void testPoint() throws IOException {
    Shape s = read("10 20");
    assertEquals(ctx.makePoint(10,20),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,read("20,10"));//check comma for y,x format
    assertEquals(s,read("20, 10"));//test space
    assertFalse(s.hasArea());
  }

  @Test
  public void testRectangle() throws IOException {
    Shape s = read("-10 -20 10 20");
    assertEquals(ctx.makeRectangle(-10, 10, -20, 20),s);
    assertEquals(s,writeThenRead(s));
    assertTrue(s.hasArea());
  }

  @Test
  public void testCircle() throws IOException {
    Shape s = read("Circle(1.23 4.56 distance=7.89)");
    assertEquals(ctx.makeCircle(1.23, 4.56, 7.89),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,read("CIRCLE( 4.56,1.23 d=7.89 )")); // use lat,lon and use 'd' abbreviation
    assertTrue(s.hasArea());
  }


  //  Looking for more tests?  Shapes are tested in TestShapes2D.

}
