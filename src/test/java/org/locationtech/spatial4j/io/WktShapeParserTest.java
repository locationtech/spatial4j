/*******************************************************************************
 * Copyright (c) 2015 ElasticSearch and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

// A derivative of commit 14bc4dee08355048d6a94e33834b919a3999a06e
//  at https://github.com/chrismale/elasticsearch

package org.locationtech.spatial4j.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.junit.Test;

import java.text.ParseException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class WktShapeParserTest extends RandomizedTest {

  final SpatialContext ctx;

  protected WktShapeParserTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public WktShapeParserTest() {
    this(SpatialContext.GEO);
  }

  protected void assertParses(String wkt, Shape expected) throws ParseException {
    assertEquals(wkt(wkt), expected);
  }

  protected Shape wkt(String wkt) throws ParseException {
    return wkt(ctx, wkt);
  }

  protected Shape wkt(SpatialContext ctx, String wkt) throws ParseException {
    return ((WKTReader) ctx.getFormats().getWktReader()).parse(wkt);
  }

  protected void assertFails(String wkt) {
    try {
      wkt(wkt);
      fail("ParseException expected");
    } catch (ParseException e) {//expected
    }
  }

  @Test
  public void testNoOp() throws ParseException {
    WKTReader wktShapeParser = (WKTReader) ctx.getFormats().getWktReader();
    assertNull(wktShapeParser.parseIfSupported(""));
    assertNull(wktShapeParser.parseIfSupported("  "));
    assertNull(wktShapeParser.parseIfSupported("BogusShape()"));
    assertNull(wktShapeParser.parseIfSupported("BogusShape"));
  }

  @Test
  public void testParsePoint() throws ParseException {
    assertParses("POINT (100 90)", ctx.makePoint(100, 90));//typical
    assertParses(" POINT (100 90) ", ctx.makePoint(100, 90));//trimmed
    assertParses("point (100 90)", ctx.makePoint(100, 90));//case indifferent
    assertParses("POINT ( 100 90 )", ctx.makePoint(100, 90));//inner spaces
    assertParses("POINT(100 90)", ctx.makePoint(100, 90));
    assertParses("POINT (-45 90 )", ctx.makePoint(-45, 90));
    Point expected = ctx.makePoint(-45.3, 80.4);
    assertParses("POINT (-45.3 80.4 )", expected);
    assertParses("POINT (-45.3 +80.4 )", expected);
    assertParses("POINT (-45.3 8.04e1 )", expected);

    assertParses("POINT EMPTY", ctx.makePoint(Double.NaN, Double.NaN));

    //other dimensions are skipped
    assertParses("POINT (100 90 2)", ctx.makePoint(100, 90));
    assertParses("POINT (100 90 2 3)", ctx.makePoint(100, 90));
    assertParses("POINT ZM ( 100 90 )", ctx.makePoint(100, 90));//ignore dimension
    assertParses("POINT ZM ( 100 90 -3 -4)", ctx.makePoint(100, 90));//ignore dimension
  }

  @Test
  public void testParsePoint_invalidDefinitions() {
    assertFails("POINT 100 90");
    assertFails("POINT (100 90");
    assertFails("POINT (100, 90)");
    assertFails("POINT 100 90)");
    assertFails("POINT (100)");
    assertFails("POINT (10f0 90)");
    assertFails("POINT (EMPTY)");

    assertFails("POINT (1 2), POINT (2 3)");
    assertFails("POINT EMPTY (1 2)");
    assertFails("POINT ZM EMPTY (1 2)");
    assertFails("POINT ZM EMPTY 1");
  }

  @Test
  public void testParseMultiPoint() throws ParseException {
    Shape s1 = ctx.getShapeFactory().multiPoint().pointXY(10, 40).build();
    assertParses("MULTIPOINT (10 40)", s1);

    Shape s4 = ctx.getShapeFactory().multiPoint()
            .pointXY(10, 40).pointXY(40, 30).pointXY(20, 20).pointXY(30, 10).build();
    assertParses("MULTIPOINT ((10 40), (40 30), (20 20), (30 10))", s4);
    assertParses("MULTIPOINT (10 40, 40 30, 20 20, 30 10)", s4);

    assertParses("MULTIPOINT Z EMPTY", ctx.getShapeFactory().multiPoint().build());
  }

  @Test
  public void testParseEnvelope() throws ParseException {
    Rectangle r = ctx.makeRectangle(ctx.makePoint(10, 25), ctx.makePoint(30, 45));
    assertParses(" ENVELOPE ( 10 , 30 , 45 , 25 ) ", r);
    assertParses("ENVELOPE(10,30,45,25) ", r);
    assertFails("ENVELOPE (10 30 45 25)");
  }

  @Test
  public void testLineStringShape() throws ParseException {
    Shape ls = ctx.getShapeFactory().lineString().pointXY(1, 10).pointXY(2, 20).pointXY(3, 30).build();
    assertParses("LINESTRING (1 10, 2 20, 3 30)", ls);

    assertParses("LINESTRING EMPTY", ctx.makeLineString(Collections.<Point>emptyList()));
  }

  @Test
  public void testMultiLineStringShape() throws ParseException {
    ShapeFactory.MultiLineStringBuilder builder = ctx.getShapeFactory().multiLineString();
    builder.add(builder.lineString().pointXY(10, 10).pointXY(20, 20).pointXY(10, 40));
    builder.add(builder.lineString().pointXY(40, 40).pointXY(30, 30).pointXY(40, 20).pointXY(30, 10));
    Shape s = builder.build();
    assertParses("MULTILINESTRING ((10 10, 20 20, 10 40),\n" +
        "(40 40, 30 30, 40 20, 30 10))", s);

    assertParses("MULTILINESTRING M EMPTY", ctx.getShapeFactory().multiLineString().build());
  }

  @Test
  public void testGeomCollection() throws ParseException {
    ShapeFactory shapeFactory = ctx.getShapeFactory();
    Shape s1 = shapeFactory.multiShape(Shape.class).add(shapeFactory.pointXY(1, 2)).build();
    Shape s2 = shapeFactory.multiShape(Shape.class)
            .add(shapeFactory.rect(1, 2, 3, 4)).add(shapeFactory.pointXY(-1, -2)).build();
    assertParses("GEOMETRYCOLLECTION (POINT (1 2) )", s1);
    assertParses("GEOMETRYCOLLECTION ( ENVELOPE(1,2,4,3), POINT(-1 -2)) ", s2);

    assertParses("GEOMETRYCOLLECTION EMPTY", shapeFactory.multiShape(Shape.class).build());

    assertParses("GEOMETRYCOLLECTION ( POINT EMPTY )",
            shapeFactory.multiShape(Shape.class).add(shapeFactory.pointXY(Double.NaN, Double.NaN)).build());
  }

  @Test
  public void testBuffer() throws ParseException {
    assertParses("BUFFER(POINT(1 2), 3)", ctx.makePoint(1, 2).getBuffered(3, ctx));
  }
}
