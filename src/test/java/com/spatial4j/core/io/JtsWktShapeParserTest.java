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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.context.jts.DatelineRule;
import com.spatial4j.core.io.jts.JtsWKTReader;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

public class JtsWktShapeParserTest extends WktShapeParserTest {

  //By extending WktShapeParserTest we inherit its test too

  final JtsSpatialContext ctx;//note: masks superclass

  public JtsWktShapeParserTest() {
    super(JtsSpatialContext.GEO);
    this.ctx = (JtsSpatialContext) super.ctx;
  }

  @Test
  public void testParsePolygon() throws ParseException {
    Shape polygonNoHoles = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(101, 0)
        .point(101, 1)
        .point(100, 2)
        .point(100, 0)
        .build();
    String polygonNoHolesSTR = "POLYGON ((100 0, 101 0, 101 1, 100 2, 100 0))";
    assertParses(polygonNoHolesSTR, polygonNoHoles);
    assertParses("POLYGON((100 0,101 0,101 1,100 2,100 0))", polygonNoHoles);

    assertParses("GEOMETRYCOLLECTION ( "+polygonNoHolesSTR+")",
        ctx.makeCollection(Arrays.asList(polygonNoHoles)));

    Shape polygonWithHoles = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(101, 0)
        .point(101, 1)
        .point(100, 1)
        .point(100, 0)
        .newHole()
        .point(100.2, 0.2)
        .point(100.8, 0.2)
        .point(100.8, 0.8)
        .point(100.2, 0.8)
        .point(100.2, 0.2)
        .endHole()
        .build();
    assertParses("POLYGON ((100 0, 101 0, 101 1, 100 1, 100 0), (100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2))", polygonWithHoles);

    GeometryFactory gf = ctx.getGeometryFactory();
    assertParses("POLYGON EMPTY", ctx.makeShape(
        gf.createPolygon(gf.createLinearRing(new Coordinate[]{}), null)
    ));
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
    assertEquals(ctx.readShapeFromWkt("POLYGON((160 0, -170 0, -170 10, 160 10, 160 0))"),
        ctx.makeRectangle(160, -170, 0, 10));
    //clockwise
    assertEquals(ctx.readShapeFromWkt("POLYGON((160 10, -170 10, -170 0, 160 0, 160 10))"),
        ctx.makeRectangle(-170, 160, 0, 10));
  }

  @Test
  public void testParseMultiPolygon() throws ParseException {
    Shape p1 = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(101, 0)//101
        .point(101, 2)//101
        .point(100, 1)
        .point(100, 0)
        .build();
    Shape p2 = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(102, 0)//102
        .point(102, 2)//102
        .point(100, 1)
        .point(100, 0)
        .build();
    Shape s = ctx.makeCollection(
        Arrays.asList(p1, p2)
    );
    assertParses("MULTIPOLYGON(" +
        "((100 0, 101 0, 101 2, 100 1, 100 0))" + ',' +
        "((100 0, 102 0, 102 2, 100 1, 100 0))" +
        ")", s);

    assertParses("MULTIPOLYGON EMPTY", ctx.makeCollection(Collections.EMPTY_LIST));
  }

  @Test
  public void testLineStringDateline() throws ParseException {
    //works because we use JTS (JtsGeometry); BufferedLineString doesn't yet do DL wrap.
    Shape s = ctx.readShapeFromWkt("LINESTRING(160 10, -170 15)");
    assertEquals(30, s.getBoundingBox().getWidth(), 0.0 );
  }

  @Test
  public void testWrapTopologyException() throws Exception {
    //test that we can catch ParseException without having to detect TopologyException too
    assert ((JtsWKTReader)ctx.getWktShapeParser()).isAutoValidate();
    try {
      ctx.readShapeFromWkt("POLYGON((0 0, 10 0, 10 20))");//doesn't connect around
      fail();
    } catch (InvalidShapeException e) {
      //expected
    }

    try {
      ctx.readShapeFromWkt("POLYGON((0 0, 10 0, 10 20, 5 -5, 0 20, 0 0))");//Topology self-intersect
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
    factory.validationRule = JtsWKTReader.ValidationRule.repairBuffer0;
    JtsSpatialContext ctx = factory.newSpatialContext();
    Shape buffer0 = ctx.readShapeFromWkt(wkt);
    assertTrue(buffer0.getArea(ctx) > 0);

    factory = new JtsSpatialContextFactory();
    factory.validationRule = JtsWKTReader.ValidationRule.repairConvexHull;
    ctx = factory.newSpatialContext();
    Shape cvxHull = ctx.readShapeFromWkt(wkt);
    assertTrue(cvxHull.getArea(ctx) > 0);

    assertEquals(SpatialRelation.CONTAINS, cvxHull.relate(buffer0));

    factory = new JtsSpatialContextFactory();
    factory.validationRule = JtsWKTReader.ValidationRule.none;
    ctx = factory.newSpatialContext();
    ctx.readShapeFromWkt(wkt);//doesn't throw
  }

}
