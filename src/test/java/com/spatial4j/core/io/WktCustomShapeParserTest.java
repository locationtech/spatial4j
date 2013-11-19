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

import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Test;

import java.text.ParseException;

public class WktCustomShapeParserTest extends WktShapeParserTest {

  class CustomShape extends PointImpl {

    private final String name;

    /**
     * A simple constructor without normalization / validation.
     */
    public CustomShape(String name) {
      super(0, 0, ctx);
      this.name = name;
    }
  }

  MyWKTShapeParser SHAPE_PARSER = new MyWKTShapeParser();

  @Test
  public void testCustomShape() throws ParseException {
    assertEquals("customShape", ((CustomShape)SHAPE_PARSER.parse("customShape()")).name);
    assertEquals("custom3d", ((CustomShape)SHAPE_PARSER.parse("custom3d ()")).name);//number supported
  }

  @Test
  public void testNextSubShapeString() throws ParseException {

    WktShapeParser.State state = SHAPE_PARSER.newState("OUTER(INNER(3, 5))");
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

  private class MyWKTShapeParser extends WktShapeParser {
    public MyWKTShapeParser() {
      super(WktCustomShapeParserTest.this.ctx);
    }

    protected State newState(String wkt) {
      //First few lines compile, despite newState() being protected. Just proving extensibility.
      WktShapeParser other = null;
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
        return new CustomShape(shapeType);
      }
      return result;
    }
  }
}
