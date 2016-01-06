/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.DatelineRule;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.io.IOException;

public class JtsWKTReaderShapeParserTest extends RandomizedTest {

  final SpatialContext ctx;
  {
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.datelineRule = DatelineRule.ccwRect;
    factory.readers.clear();
    factory.readers.add( JtsWKTReaderShapeParser.class );
    ctx = factory.newSpatialContext();
  }

  @Test
  public void wktGeoPt() throws IOException {
    Shape s = read("Point(-160 30)");
    assertEquals(ctx.makePoint(-160,30),s);
  }

  private Shape read(String value) {
    return ctx.getFormats().read(value);
  }

  @Test
  public void wktGeoRect() throws IOException {
    //REMEMBER: Polygon WKT's outer ring is counter-clockwise order. If you accidentally give the other direction,
    // JtsSpatialContext will give the wrong result for a rectangle crossing the dateline.

    // In these two tests, we give the same set of points, one that does not cross the dateline, and the 2nd does. The
    // order is counter-clockwise in both cases as it should be.

    Shape sNoDL = read("Polygon((-170 30, -170 15,  160 15,  160 30, -170 30))");
    Rectangle expectedNoDL = ctx.makeRectangle(-170, 160, 15, 30);
    assertTrue(!expectedNoDL.getCrossesDateLine());
    assertEquals(expectedNoDL,sNoDL);

    Shape sYesDL = read("Polygon(( 160 30,  160 15, -170 15, -170 30,  160 30))");
    Rectangle expectedYesDL = ctx.makeRectangle(160, -170, 15, 30);
    assertTrue(expectedYesDL.getCrossesDateLine());
    assertEquals(expectedYesDL,sYesDL);

  }


  @Test
  public void testWrapTopologyException() {
    try {
      read("POLYGON((0 0, 10 0, 10 20))");//doesn't connect around
      fail();
    } catch (InvalidShapeException e) {
      //expected
    }

    try {
      read("POLYGON((0 0, 10 0, 10 20, 5 -5, 0 20, 0 0))");//Topology self-intersect
      fail();
    } catch (InvalidShapeException e) {
      //expected
    }
  }

}
