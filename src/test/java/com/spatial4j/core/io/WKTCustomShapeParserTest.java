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

public class WKTCustomShapeParserTest extends WKTShapeParserTest {

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

  WKTShapeParser SHAPE_PARSER = new WKTShapeParser(ctx) {
    @Override
    protected Shape parseShapeByType(String shapeType) throws ParseException {
      Shape result = super.parseShapeByType(shapeType);
      if (result == null && shapeType.contains("custom")) {
        expect('(');
        expect(')');
        return new CustomShape(shapeType);
      }
      return result;
    }
  };

  @Test
  public void testCustomShape() throws ParseException {
    assertEquals("customShape", ((CustomShape)SHAPE_PARSER.parse("customShape()")).name);
    assertEquals("custom3d", ((CustomShape)SHAPE_PARSER.parse("custom3d ()")).name);//number supported
  }
}
