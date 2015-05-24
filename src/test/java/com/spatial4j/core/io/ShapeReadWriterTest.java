/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Ryan McKinley - initial API and implementation
 *    David Smiley
 ******************************************************************************/

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;


@SuppressWarnings("unchecked")
public class ShapeReadWriterTest extends RandomizedTest {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
            $(SpatialContext.GEO),
            $(JtsSpatialContext.GEO)
    ));
  }

  private final SpatialContext ctx;

  public ShapeReadWriterTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  private <T extends Shape> T writeThenRead( T s ) throws IOException {
    String buff = ctx.toString( s );
    return (T) ctx.readShape( buff );
  }

  @Test
  public void testPoint() throws IOException {
    Shape s = ctx.readShape("10 20");
    assertEquals(ctx.makePoint(10,20),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,ctx.readShape("20,10"));//check comma for y,x format
    assertEquals(s,ctx.readShape("20, 10"));//test space
    assertFalse(s.hasArea());
  }

  @Test
  public void testRectangle() throws IOException {
    Shape s = ctx.readShape("-10 -20 10 20");
    assertEquals(ctx.makeRectangle(-10, 10, -20, 20),s);
    assertEquals(s,writeThenRead(s));
    assertTrue(s.hasArea());
  }

  @Test
  public void testCircle() throws IOException {
    Shape s = ctx.readShape("Circle(1.23 4.56 distance=7.89)");
    assertEquals(ctx.makeCircle(1.23, 4.56, 7.89),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,ctx.readShape("CIRCLE( 4.56,1.23 d=7.89 )")); // use lat,lon and use 'd' abbreviation
    assertTrue(s.hasArea());
  }


  //  Looking for more tests?  Shapes are tested in TestShapes2D.

}
