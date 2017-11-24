/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.io.WKTReader;
import org.locationtech.spatial4j.shape.impl.PointImpl;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.locationtech.jts.geom.*;
import org.junit.Test;
import org.locationtech.spatial4j.util.Geom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;
import static org.locationtech.spatial4j.shape.SpatialRelation.DISJOINT;
import static org.locationtech.spatial4j.shape.SpatialRelation.INTERSECTS;
import static org.locationtech.spatial4j.shape.SpatialRelation.WITHIN;

/** Tests {@link org.locationtech.spatial4j.shape.jts.JtsGeometry} and some other code related
 * to {@link org.locationtech.spatial4j.context.jts.JtsSpatialContext}.
 */
public class JtsGeometryTest extends AbstractTestShapes {

  private final String POLY_STR = "Polygon((-10 30, -40 40, -10 -20, 40 20, 0 0, -10 30))";
  private JtsGeometry POLY_SHAPE;
  private final int DL_SHIFT = 180;//since POLY_SHAPE contains 0 0, I know a shift of 180 will make it cross the DL.
  private JtsGeometry POLY_SHAPE_DL;//POLY_SHAPE shifted by DL_SHIFT to cross the dateline
  final JtsSpatialContext ctxNotGeo;

  public JtsGeometryTest() throws ParseException {
    super(JtsSpatialContext.GEO);
    POLY_SHAPE = (JtsGeometry) wkt(ctx, POLY_STR);

    if (ctx.isGeo()) {
      POLY_SHAPE_DL = shiftPoly(POLY_SHAPE, DL_SHIFT);
      assertTrue(POLY_SHAPE_DL.getBoundingBox().getCrossesDateLine());
    }

    JtsSpatialContextFactory ctxFactory = new JtsSpatialContextFactory();
    ctxFactory.geo = false;
    ctxFactory.worldBounds = new RectangleImpl(-1000, 1000, -1000, 1000, null);
    ctxNotGeo = ctxFactory.newSpatialContext();
  }

  private JtsGeometry shiftPoly(JtsGeometry poly, final int lon_shift) throws ParseException {
    final Random random = RandomizedContext.current().getRandom();
    Geometry pGeom = poly.getGeom();
    assertTrue(pGeom.isValid());
    //shift 180 to the right
    pGeom = (Geometry) pGeom.clone();
    pGeom.apply(new CoordinateFilter() {
      @Override
      public void filter(Coordinate coord) {
        coord.x = normX(coord.x + lon_shift);
        if (ctx.isGeo() && Math.abs(coord.x) == 180 && random.nextBoolean())
          coord.x = - coord.x;//invert sign of dateline boundary some of the time
      }
    });
    pGeom.geometryChanged();
    assertFalse(pGeom.isValid());
    return (JtsGeometry) wkt(ctx, pGeom.toText());
  }

  @Test
  public void testRelations() throws ParseException {
    testRelations(false);
    testRelations(true);
  }

  public void testRelations(boolean prepare) throws ParseException {
    assert !((JtsSpatialContext)ctx).isAutoIndex();
    //base polygon
    JtsGeometry base = (JtsGeometry) wkt(ctx, "POLYGON((0 0, 10 0, 5 5, 0 0))");
    //shares only "10 0" with base
    JtsGeometry polyI = (JtsGeometry) wkt(ctx, "POLYGON((10 0, 20 0, 15 5, 10 0))");
    //within base: differs from base by one point is within
    JtsGeometry polyW = (JtsGeometry) wkt(ctx, "POLYGON((0 0, 9 0, 5 5, 0 0))");
    //a boundary point of base
    Point pointB = ctx.makePoint(0, 0);
    //a shared boundary line of base
    JtsGeometry lineB = (JtsGeometry) wkt(ctx, "LINESTRING(0 0, 10 0)");
    //a line sharing only one point with base
    JtsGeometry lineI = (JtsGeometry) wkt(ctx, "LINESTRING(10 0, 20 0)");

    if (prepare) base.index();
    assertRelation(CONTAINS, base, base);//preferred result as there is no EQUALS
    assertRelation(INTERSECTS, base, polyI);
    assertRelation(CONTAINS, base, polyW);
    assertRelation(CONTAINS, base, pointB);
    assertRelation(CONTAINS, base, lineB);
    assertRelation(INTERSECTS, base, lineI);
    if (prepare) lineB.index();
    assertRelation(CONTAINS, lineB, lineB);//line contains itself
    assertRelation(CONTAINS, lineB, pointB);
  }

  @Test
  public void testEmpty() throws ParseException {
    Shape emptyGeom = wkt(ctx, "POLYGON EMPTY");
    testEmptiness(emptyGeom);
    assertRelation("EMPTY", DISJOINT, emptyGeom, POLY_SHAPE);
  }

  @Test
  public void testArea() {
    //simple bbox
    Rectangle r = randomRectangle(20);
    JtsSpatialContext ctxJts = (JtsSpatialContext) ctx;
    JtsGeometry rPoly = ctxJts.makeShape(ctxJts.getGeometryFrom(r), false, false);
    assertEquals(r.getArea(null), rPoly.getArea(null), 0.0);
    assertEquals(r.getArea(ctx), rPoly.getArea(ctx), 0.000001);//same since fills 100%

    assertEquals(1300, POLY_SHAPE.getArea(null), 0.0);

    //fills 27%
    assertEquals(0.27, POLY_SHAPE.getArea(ctx) / POLY_SHAPE.getBoundingBox().getArea(ctx), 0.009);
    assertTrue(POLY_SHAPE.getBoundingBox().getArea(ctx) > POLY_SHAPE.getArea(ctx));
  }

  @Test
  @Repeat(iterations = 100)
  public void testPointAndRectIntersect() {
    Rectangle r = randomRectangle(5);

    assertJtsConsistentRelate(r);
    assertJtsConsistentRelate(r.getCenter());
  }

  @Test
  public void testRegressions() {
    assertJtsConsistentRelate(new PointImpl(-10, 4, ctx));//PointImpl not JtsPoint, and CONTAINS
    assertJtsConsistentRelate(new PointImpl(-15, -10, ctx));//point on boundary
    assertJtsConsistentRelate(ctx.makeRectangle(135, 180, -10, 10));//180 edge-case
  }

  @Test
  public void testWidthGreaterThan180() throws ParseException {
    //does NOT cross the dateline but is a wide shape >180
    JtsGeometry jtsGeo = (JtsGeometry) wkt(ctx, "POLYGON((-161 49, 0 49, 20 49, 20 89.1, 0 89.1, -161 89.2, -161 49))");
    assertEquals(161+20,jtsGeo.getBoundingBox().getWidth(), 0.001);

    //shift it to cross the dateline and check that it's still good
    jtsGeo = shiftPoly(jtsGeo, 180);
    assertEquals(161+20,jtsGeo.getBoundingBox().getWidth(), 0.001);
  }

  private void assertJtsConsistentRelate(Shape shape) {
    IntersectionMatrix expectedM = POLY_SHAPE.getGeom().relate(((JtsSpatialContext) ctx).getGeometryFrom(shape));
    SpatialRelation expectedSR = JtsGeometry.intersectionMatrixToSpatialRelation(expectedM);
    //JTS considers a point on a boundary INTERSECTS, not CONTAINS
    if (expectedSR == SpatialRelation.INTERSECTS && shape instanceof Point)
      expectedSR = SpatialRelation.CONTAINS;
    assertRelation(null, expectedSR, POLY_SHAPE, shape);

    if (ctx.isGeo()) {
      //shift shape, set to shape2
      Shape shape2;
      if (shape instanceof Rectangle) {
        Rectangle r = (Rectangle) shape;
        shape2 = makeNormRect(r.getMinX() + DL_SHIFT, r.getMaxX() + DL_SHIFT, r.getMinY(), r.getMaxY());
      } else if (shape instanceof Point) {
        Point p = (Point) shape;
        shape2 = ctx.makePoint(normX(p.getX() + DL_SHIFT), p.getY());
      } else {
        throw new RuntimeException(""+shape);
      }

      assertRelation(null, expectedSR, POLY_SHAPE_DL, shape2);
    }
  }

  @Test
  public void testRussia() throws IOException, ParseException {
    final String wktStr = readFirstLineFromRsrc("/russia.wkt.txt");
    //Russia exercises JtsGeometry fairly well because of these characteristics:
    // * a MultiPolygon
    // * crosses the dateline
    // * has coordinates needing normalization (longitude +180.000xxx)

    //TODO THE RUSSIA TEST DATA SET APPEARS CORRUPT
    // But this test "works" anyhow, and exercises a ton.
    //Unexplained holes revealed via KML export:
    // TODO Test contains: 64°12'44.82"N    61°29'5.20"E
    //  64.21245  61.48475
    // FAILS
    //assertRelation(null,SpatialRelation.CONTAINS, shape, ctx.makePoint(61.48, 64.21));

    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.normWrapLongitude = true;
    // either we need to not use JTS's MultiPolygon, or we need to set allowMultiOverlap=true
    if (randomBoolean()) {
      factory.allowMultiOverlap = true;
    } else {
      factory.useJtsMulti = false;
    }
    JtsSpatialContext ctx = factory.newSpatialContext();

    Shape shape = wkt(ctx, wktStr);
    //System.out.println("Russia Area: "+shape.getArea(ctx));
  }

  @Test
  public void testFiji() throws IOException, ParseException {
    //Fiji is a group of islands crossing the dateline.
    String wktStr = readFirstLineFromRsrc("/fiji.wkt.txt");

    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.normWrapLongitude = true;
    JtsSpatialContext ctx = factory.newSpatialContext();

    Shape shape = wkt(ctx, wktStr);

    assertRelation(null,SpatialRelation.CONTAINS, shape,
            ctx.makePoint(-179.99,-16.9));
    assertRelation(null,SpatialRelation.CONTAINS, shape,
            ctx.makePoint(+179.99,-16.9));
    assertTrue(shape.getBoundingBox().getWidth() < 5);//smart bbox
    System.out.println("Fiji Area: "+shape.getArea(ctx));
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

  @Test
  public void testNarrowGeometryCollection() {
    // test points
    GeometryCollection gcol = Geom.build()
      .point(1, 1).point()
      .point(2, 3).point()
      .toCollection();
    assertFalse(gcol instanceof MultiPoint);

    JtsGeometry geom = JtsSpatialContext.GEO.makeShape(gcol);
    assertTrue(geom.getGeom() instanceof MultiPoint);

    // test lines
    gcol = Geom.build()
      .point(1,1).point(2,2).lineString()
      .point(3,3).point(4,4).lineString()
      .toCollection();

    geom = JtsSpatialContext.GEO.makeShape(gcol);
    assertTrue(geom.getGeom() instanceof MultiLineString);

    // test polygons
    gcol = Geom.build()
      .point(1,1).point().buffer(1)
      .point(2,3).point().buffer(1)
      .toCollection();

    geom = JtsSpatialContext.GEO.makeShape(gcol);
    assertTrue(geom.getGeom() instanceof MultiPolygon);

    // test heterogenous
    gcol = Geom.build()
        .point(0,0).point()
        .point(1,1).point(2,2).lineString()
        .toCollection();
    try {
      JtsSpatialContext.GEO.makeShape(gcol);
      fail("heterogenous geometry collection should throw exception");
    }
    catch(IllegalArgumentException expected) {
    }

  }

  @Test
  public void testPolyRelatesToCircle() throws ParseException {
    // The polygon is a triangle with a 90-degree angle and two equal sides, and with
    // a rectangular hole in the middle.
    Shape poly = wkt(ctxNotGeo, "POLYGON ((1 1, 1 50, 50 1, 1 1), (10 10, 10 15, 15 15, 15 10, 10 10))");

    assertRelation(WITHIN, poly, ctxNotGeo.makeCircle(25, 25, 40));
    assertRelation(CONTAINS, poly, ctxNotGeo.makeCircle(10, 25, 5));
    assertRelation(DISJOINT, poly, ctxNotGeo.makeCircle(35, 35, 5));
    assertRelation(DISJOINT, poly, ctxNotGeo.makeCircle(12, 12, 1)); // inside the hole

    // Intersects, or almost intersects and is something else
    //                                                                  The circle...
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(25, 25, 34)); // not *quite* within
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(30, 30, 10)); // crosses into the long angle
    assertRelation(DISJOINT,   poly, ctxNotGeo.makeCircle(30, 30, 5)); // almost crosses into the long angle
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(25, -5, 10)); // crosses into the bottom edge
    assertRelation(DISJOINT,   poly, ctxNotGeo.makeCircle(25, -5, 1)); // almost crosses into the bottom edge
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(0, 0, 10)); // encloses a corner
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(10, 35, 5)); // inside but sticks out at the angle
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(12, 12, 10)); // encloses the hole but otherwise inside the triangle
  }

  @Test
  public void testMultiLineStringRelatesToCircle() throws org.locationtech.jts.io.ParseException {
    // use JTS WKTReader to ensure we get one Geometry in the end
    org.locationtech.jts.io.WKTReader wktReader = new org.locationtech.jts.io.WKTReader();
    Shape poly = ctxNotGeo.makeShape(wktReader.read("MULTILINESTRING ((5 20, 5 5, 20 5), (20 25, 30 15))"));
    assertEquals(JtsGeometry.class, poly.getClass());

    assertRelation(WITHIN, poly, ctxNotGeo.makeCircle(15, 15, 20));
    assertRelation(DISJOINT, poly, ctxNotGeo.makeCircle(15, 15, 5)); // much smaller now; doesn't touch anything

    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(5, 5, 16)); // circle encloses the left lineString
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(25, 20, 10)); // circle encloses the right lineString
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(5, 20, 1)); // circle encloses first point
    assertRelation(INTERSECTS, poly, ctxNotGeo.makeCircle(26, 21, 2)); // only intersects an edge of 2nd
    // not CONTAINS is impossible with a circle; line strings don't contain anything
  }

  private Shape wkt(SpatialContext ctx, String wkt) throws ParseException {
    return ((WKTReader) ctx.getFormats().getWktReader()).parse(wkt);
  }

}
