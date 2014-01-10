/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Random;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;

/** Tests {@link com.spatial4j.core.shape.jts.JtsGeometry} and some other code related
 * to {@link com.spatial4j.core.context.jts.JtsSpatialContext}.
 */
public class JtsGeometryTest extends AbstractTestShapes {

  private final String POLY_STR = "Polygon((-10 30, -40 40, -10 -20, 40 20, 0 0, -10 30))";
  private JtsGeometry POLY_SHAPE;
  private final int DL_SHIFT = 180;//since POLY_SHAPE contains 0 0, I know a shift of 180 will make it cross the DL.
  private JtsGeometry POLY_SHAPE_DL;//POLY_SHAPE shifted by DL_SHIFT to cross the dateline

  public JtsGeometryTest() throws ParseException {
    super(JtsSpatialContext.GEO);
    POLY_SHAPE = (JtsGeometry) ctx.readShapeFromWkt(POLY_STR);

    if (ctx.isGeo()) {
      POLY_SHAPE_DL = shiftPoly(POLY_SHAPE, DL_SHIFT);
      assertTrue(POLY_SHAPE_DL.getBoundingBox().getCrossesDateLine());
    }
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
    return (JtsGeometry) ctx.readShapeFromWkt(pGeom.toText());
  }

  @Test
  public void testRelations() throws ParseException {
    testRelations(false);
    testRelations(true);
  }
  public void testRelations(boolean prepare) throws ParseException {
    assert !((JtsWktShapeParser)ctx.getWktShapeParser()).isAutoIndex();
    //base polygon
    JtsGeometry base = (JtsGeometry) ctx.readShapeFromWkt("POLYGON((0 0, 10 0, 5 5, 0 0))");
    //shares only "10 0" with base
    JtsGeometry polyI = (JtsGeometry) ctx.readShapeFromWkt("POLYGON((10 0, 20 0, 15 5, 10 0))");
    //within base: differs from base by one point is within
    JtsGeometry polyW = (JtsGeometry) ctx.readShapeFromWkt("POLYGON((0 0, 9 0, 5 5, 0 0))");
    //a boundary point of base
    Point pointB = ctx.makePoint(0, 0);
    //a shared boundary line of base
    JtsGeometry lineB = (JtsGeometry) ctx.readShapeFromWkt("LINESTRING(0 0, 10 0)");
    //a line sharing only one point with base
    JtsGeometry lineI = (JtsGeometry) ctx.readShapeFromWkt("LINESTRING(10 0, 20 0)");

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
    Shape emptyGeom = ctx.readShapeFromWkt("POLYGON EMPTY");
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
    JtsGeometry jtsGeo = (JtsGeometry) ctx.readShapeFromWkt("POLYGON((-161 49, 0 49, 20 49, 20 89.1, 0 89.1, -161 89.2, -161 49))");
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

    JtsSpatialContext ctx = factory.newSpatialContext();

    Shape shape = ctx.readShapeFromWkt(wktStr);
    //System.out.println("Russia Area: "+shape.getArea(ctx));
  }

  @Test
  public void testFiji() throws IOException, ParseException {
    //Fiji is a group of islands crossing the dateline.
    String wktStr = readFirstLineFromRsrc("/fiji.wkt.txt");

    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.normWrapLongitude = true;
    JtsSpatialContext ctx = factory.newSpatialContext();

    Shape shape = ctx.readShapeFromWkt(wktStr);

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
}
