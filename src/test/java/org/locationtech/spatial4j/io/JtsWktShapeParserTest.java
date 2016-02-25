/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.jts.DatelineRule;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.context.jts.ValidationRule;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JtsWktShapeParserTest extends WktShapeParserTest {

  //By extending WktShapeParserTest we inherit its test too

  final JtsSpatialContext ctx;//note: masks superclass

  public JtsWktShapeParserTest() {
    super(createSpatialContext());
    this.ctx = (JtsSpatialContext) super.ctx;
  }

  static JtsSpatialContext createSpatialContext() {
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();

    factory.useJtsMulti = false;
    return factory.newSpatialContext();
  }

  @Test
  public void testParsePolygon() throws ParseException {
    Shape polygonNoHoles = ctx.getShapeFactory().polygon()
        .pointXY(100, 0)
        .pointXY(101, 0)
        .pointXY(101, 1)
        .pointXY(100, 2)
        .pointXY(100, 0)
        .build();
    String polygonNoHolesSTR = "POLYGON ((100 0, 101 0, 101 1, 100 2, 100 0))";
    assertParses(polygonNoHolesSTR, polygonNoHoles);
    assertParses("POLYGON((100 0,101 0,101 1,100 2,100 0))", polygonNoHoles);

    assertParses("GEOMETRYCOLLECTION ( "+polygonNoHolesSTR+")",
        ctx.makeCollection(Arrays.asList(polygonNoHoles)));

    Shape polygonWithHoles = ctx.getShapeFactory().polygon()
        .pointXY(100, 0)
        .pointXY(101, 0)
        .pointXY(101, 1)
        .pointXY(100, 1)
        .pointXY(100, 0)
        .hole()
        .pointXY(100.2, 0.2)
        .pointXY(100.8, 0.2)
        .pointXY(100.8, 0.8)
        .pointXY(100.2, 0.8)
        .pointXY(100.2, 0.2)
        .endHole()
        .build();
    assertParses("POLYGON ((100 0, 101 0, 101 1, 100 1, 100 0), (100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2))", polygonWithHoles);

    assertParses("POLYGON EMPTY", ctx.getShapeFactory().polygon().build());
  }

  @Test
  public void testPolyToRect() throws ParseException {
    //poly is a rect (no dateline issue)
    assertParses("POLYGON((0 5, 10 5, 10 20, 0 20, 0 5))", ctx.makeRectangle(0, 10, 5, 20));
  }

  @Test
  public void polyToRect180Rule() throws ParseException {
    //crosses dateline
    Rectangle expected = ctx.makeRectangle(160, -170, 0, 10);
    //counter-clockwise
    assertParses("POLYGON((160 0, -170 0, -170 10, 160 10, 160 0))", expected);
    //clockwise
    assertParses("POLYGON((160 10, -170 10, -170 0, 160 0, 160 10))", expected);
  }

  @Test
  public void polyToRectCcwRule() throws ParseException {
    JtsSpatialContext ctx = new JtsSpatialContextFactory() { { datelineRule = DatelineRule.ccwRect;} }.newSpatialContext();
    //counter-clockwise
    assertEquals(wkt(ctx, "POLYGON((160 0, -170 0, -170 10, 160 10, 160 0))"),
        ctx.makeRectangle(160, -170, 0, 10));
    //clockwise
    assertEquals(wkt(ctx, "POLYGON((160 10, -170 10, -170 0, 160 0, 160 10))"),
        ctx.makeRectangle(-170, 160, 0, 10));
  }

  @Test
  public void testParseMultiPolygon() throws ParseException {
    ShapeFactory.MultiPolygonBuilder multiPolygonBuilder = ctx.getShapeFactory().multiPolygon();
    multiPolygonBuilder.add(multiPolygonBuilder.polygon()
        .pointXY(100, 0)
        .pointXY(101, 0)//101
        .pointXY(101, 2)//101
        .pointXY(100, 1)
        .pointXY(100, 0));
    multiPolygonBuilder.add(multiPolygonBuilder.polygon()
        .pointXY(  0, 0)
        .pointXY(  2, 0)
        .pointXY(  2, 2)
        .pointXY(  0, 1)
        .pointXY(  0, 0));
    Shape s = multiPolygonBuilder.build();
    assertParses("MULTIPOLYGON(" +
        "((100 0, 101 0, 101 2, 100 1, 100 0))" + ',' +
        "((0 0, 2 0, 2 2, 0 1, 0 0))" +
        ")", s);

    assertParses("MULTIPOLYGON EMPTY", ctx.getShapeFactory().multiPolygon().build());
  }

  @Test
  public void testLineStringDateline() throws ParseException {
    //works because we use JTS (JtsGeometry); BufferedLineString doesn't yet do DL wrap.
    Shape s = wkt("LINESTRING(160 10, -170 15)");
    assertEquals(30, s.getBoundingBox().getWidth(), 0.0 );
  }

  @Test
  public void testWrapTopologyException() throws Exception {
    //test that we can catch ParseException without having to detect TopologyException too
    assert ctx.getValidationRule() != ValidationRule.none;
    try {
      wkt("POLYGON((0 0, 10 0, 10 20))");
      fail();
    } catch (InvalidShapeException e) {
      //expected
    }

    try {
      wkt("POLYGON((0 0, 10 0, 10 20, 5 -5, 0 20, 0 0))");
      fail();
    } catch (InvalidShapeException e) {
      //expected
    }
  }

  @Test
  public void testPolygonRepair() throws ParseException {
    //because we're going to test validation
    System.setProperty(JtsGeometry.SYSPROP_ASSERT_VALIDATE, "false");


    //note: doesn't repair all cases; this case isn't:
    //ctx.readShapeFromWkt("POLYGON((0 0, 10 0, 10 20))");//doesn't connect around
    String wkt = "POLYGON((0 0, 10 0, 10 20, 5 -5, 0 20, 0 0))";//Topology self-intersect

    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.validationRule = ValidationRule.repairBuffer0;
    JtsSpatialContext ctx = factory.newSpatialContext();
    Shape buffer0 = wkt(ctx,wkt);
    assertTrue(buffer0.getArea(ctx) > 0);

    factory = new JtsSpatialContextFactory();
    factory.validationRule = ValidationRule.repairConvexHull;
    ctx = factory.newSpatialContext();
    Shape cvxHull = wkt(ctx,wkt);
    assertTrue(cvxHull.getArea(ctx) > 0);

    assertEquals(SpatialRelation.CONTAINS, cvxHull.relate(buffer0));

    factory = new JtsSpatialContextFactory();
    factory.validationRule = ValidationRule.none;
    ctx = factory.newSpatialContext();
    wkt(ctx,wkt);
  }

}
