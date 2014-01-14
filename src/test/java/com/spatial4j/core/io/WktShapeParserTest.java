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

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

public class WktShapeParserTest extends RandomizedTest {

  final SpatialContext ctx;

  protected WktShapeParserTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public WktShapeParserTest() {
    this(SpatialContext.GEO);
  }

  protected void assertParses(String wkt, Shape expected) throws ParseException {
    assertEquals(ctx.readShapeFromWkt(wkt), expected);
  }

  protected void assertFails(String wkt) {
    try {
      ctx.readShapeFromWkt(wkt);
      fail("ParseException expected");
    } catch (ParseException e) {//expected
    }
  }

  @Test
  public void testNoOp() throws ParseException {
    WktShapeParser wktShapeParser = ctx.getWktShapeParser();
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
    Shape s1 = ctx.makeCollection(Collections.singletonList(ctx.makePoint(10, 40)));
    assertParses("MULTIPOINT (10 40)", s1);

    Shape s4 = ctx.makeCollection(Arrays.asList(
        ctx.makePoint(10, 40), ctx.makePoint(40, 30),
        ctx.makePoint(20, 20), ctx.makePoint(30, 10)));
    assertParses("MULTIPOINT ((10 40), (40 30), (20 20), (30 10))", s4);
    assertParses("MULTIPOINT (10 40, 40 30, 20 20, 30 10)", s4);

    assertParses("MULTIPOINT Z EMPTY", ctx.makeCollection(Collections.EMPTY_LIST));
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
    Point p1 = ctx.makePoint(1, 10);
    Point p2 = ctx.makePoint(2, 20);
    Point p3 = ctx.makePoint(3, 30);
    Shape ls = ctx.makeLineString(Arrays.asList(p1, p2, p3));
    assertParses("LINESTRING (1 10, 2 20, 3 30)", ls);

    assertParses("LINESTRING EMPTY", ctx.makeLineString(Collections.<Point>emptyList()));
  }

  @Test
  public void testMultiLineStringShape() throws ParseException {
    Shape s = ctx.makeCollection(Arrays.asList(
       ctx.makeLineString(Arrays.asList(
            ctx.makePoint(10, 10), ctx.makePoint(20, 20), ctx.makePoint(10, 40))),
        ctx.makeLineString(Arrays.asList(
            ctx.makePoint(40, 40), ctx.makePoint(30, 30), ctx.makePoint(40, 20), ctx.makePoint(30, 10)))
    ));
    assertParses("MULTILINESTRING ((10 10, 20 20, 10 40),\n" +
        "(40 40, 30 30, 40 20, 30 10))", s);

    assertParses("MULTILINESTRING M EMPTY", ctx.makeCollection(Collections.EMPTY_LIST));
  }

  @Test
  public void testGeomCollection() throws ParseException {
    Shape s1 = ctx.makeCollection(Arrays.asList(ctx.makePoint(1, 2)));
    Shape s2 = ctx.makeCollection(
        Arrays.asList(ctx.makeRectangle(1, 2, 3, 4),
            ctx.makePoint(-1, -2)) );
    assertParses("GEOMETRYCOLLECTION (POINT (1 2) )", s1);
    assertParses("GEOMETRYCOLLECTION ( ENVELOPE(1,2,4,3), POINT(-1 -2)) ", s2);

    assertParses("GEOMETRYCOLLECTION EMPTY", ctx.makeCollection(Collections.EMPTY_LIST));

    assertParses("GEOMETRYCOLLECTION ( POINT EMPTY )",
        ctx.makeCollection(Arrays.asList(ctx.makePoint(Double.NaN, Double.NaN))));
  }

  @Test
  public void testBuffer() throws ParseException {
    assertParses("BUFFER(POINT(1 2), 3)", ctx.makePoint(1, 2).getBuffered(3, ctx));
  }
}
