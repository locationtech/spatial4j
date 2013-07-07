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
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.text.ParseException;

public class WKTShapeParserTest extends RandomizedTest {
  SpatialContext ctx = SpatialContext.GEO;
  WKTShapeParser SHAPE_PARSER = new WKTShapeParser(ctx);

  protected void assertParses(String wkt, Shape expected) throws ParseException {
    assertEquals(SHAPE_PARSER.parse(wkt), expected);
  }

  protected void assertFails(String wkt) {
    try {
      SHAPE_PARSER.parse(wkt);
      fail("ParseException expected");
    } catch (ParseException e) {//expected
    }
  }

  @Test
  public void testNoOp() throws ParseException {
    assertEquals(null, SHAPE_PARSER.parseIfSupported(""));
    assertEquals(null, SHAPE_PARSER.parseIfSupported("  "));
    assertEquals(null, SHAPE_PARSER.parseIfSupported("TestShape()"));
  }

  @Test
  public void testParsePoint() throws ParseException {
    assertParses("POINT (100 90)", ctx.makePoint(100, 90));//typical
    assertParses(" POINT (100 90) ", ctx.makePoint(100, 90));//trimmed
    assertParses("point (100 90)", ctx.makePoint(100, 90));//case indifferent
    assertParses("POINT ( 100 90 )", ctx.makePoint(100, 90));//inner spaces
    assertParses("POINT(100 90)", ctx.makePoint(100, 90));
    assertParses("POINT (-45 90 )", ctx.makePoint(-45, 90));
    assertParses("POINT (-45.3 80.4 )", ctx.makePoint(-45.3, 80.4));
  }

  @Test
  public void testParseEnvelope() throws ParseException {
    Rectangle r = ctx.makeRectangle(ctx.makePoint(10, 25), ctx.makePoint(30, 45));
    assertParses("ENVELOPE (10, 30, 45, 25)", r);
  }

  @Test
  public void testParsePoint_invalidDefinitions() {
    assertFails("POINT 100 90");
    assertFails("POINT (100 90");
    assertFails("POINT 100 90)");
    assertFails("POINT (100)");
    assertFails("POINT (10f0 90)");
  }

  @Test
  public void testNextSubShapeString() throws ParseException {
    SHAPE_PARSER.rawString = "OUTER(INNER(3, 5))";
    SHAPE_PARSER.offset = 0;

    assertEquals("OUTER(INNER(3, 5))", SHAPE_PARSER.nextSubShapeString());
    assertEquals("OUTER(INNER(3, 5))".length(), SHAPE_PARSER.offset);

    SHAPE_PARSER.offset = "OUTER(".length();
    assertEquals("INNER(3, 5)", SHAPE_PARSER.nextSubShapeString());
    assertEquals("OUTER(INNER(3, 5)".length(),  SHAPE_PARSER.offset);

    SHAPE_PARSER.offset = "OUTER(INNER(".length();
    assertEquals("3", SHAPE_PARSER.nextSubShapeString());
    assertEquals("OUTER(INNER(3".length(),  SHAPE_PARSER.offset);
  }

}
