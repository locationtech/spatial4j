/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.impl.PointImpl;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class WktCustomShapeParserTest extends WktShapeParserTest {

  static class CustomShape extends PointImpl {

    private final String name;

    /**
     * A simple constructor without normalization / validation.
     */
    public CustomShape(String name, SpatialContext ctx) {
      super(0, 0, ctx);
      this.name = name;
    }
  }

  public WktCustomShapeParserTest() {
    super(makeCtx());
  }

  private static SpatialContext makeCtx() {
    SpatialContextFactory factory = new SpatialContextFactory();
    factory.readers.clear();
    factory.readers.add( MyWKTShapeParser.class );
    return factory.newSpatialContext();
  }

  @Test
  public void testCustomShape() throws ParseException {
    assertEquals("customShape", ((CustomShape) wkt("customShape()")).name);
    assertEquals("custom3d", ((CustomShape) wkt("custom3d ()")).name);//number supported
  }

  @Test
  public void testNextSubShapeString() throws ParseException {

    WKTReader.State state = ((WKTReader)ctx.getFormats().getWktReader()).newState("OUTER(INNER(3, 5))");
    state.offset = 0;

    assertEquals("OUTER(INNER(3, 5))", state.nextSubShapeString());
    assertEquals("OUTER(INNER(3, 5))".length(), state.offset);

    state.offset = "OUTER(".length();
    assertEquals("INNER(3, 5)", state.nextSubShapeString());
    assertEquals("OUTER(INNER(3, 5)".length(), state.offset);

    state.offset = "OUTER(INNER(".length();
    assertEquals("3", state.nextSubShapeString());
    assertEquals("OUTER(INNER(3".length(), state.offset);
  }

  public static class MyWKTShapeParser extends WKTReader {
    public MyWKTShapeParser(SpatialContext ctx, SpatialContextFactory factory) {
      super(ctx, factory);
    }

    @Override
    protected State newState(String wkt) {
      //First few lines compile, despite newState() being protected. Just proving extensibility.
      WKTReader other = null;
      if (false)
        other.newState(wkt);

      return new State(wkt);
    }

    @Override
    public Shape parseShapeByType(State state, String shapeType) throws ParseException {
      Shape result = super.parseShapeByType(state, shapeType);
      if (result == null && shapeType.contains("custom")) {
        state.nextExpect('(');
        state.nextExpect(')');
        return new CustomShape(shapeType, ctx);
      }
      return result;
    }
  }
}
