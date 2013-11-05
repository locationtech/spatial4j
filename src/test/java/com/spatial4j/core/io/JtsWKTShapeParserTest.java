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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;

public class JtsWKTShapeParserTest extends WKTShapeParserTest {

  //By extending WKTShapeParserTest we inherit its test too


  JtsSpatialContext ctx;//note masks superclass

  public JtsWKTShapeParserTest() {
    this.ctx = JtsSpatialContext.GEO;
    super.ctx = ctx;
    SHAPE_PARSER = new JtsWKTShapeParser(ctx);
  }


  @Test
  public void testParsePolygon() throws ParseException {
    Shape polygonNoHoles = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(101, 0)
        .point(101, 1)
        .point(100, 1)
        .point(100, 0)
        .build();
    assertParses("POLYGON ((100 0, 101 0, 101 1, 100 1, 100 0))", polygonNoHoles);
    assertParses("POLYGON((100 0,101 0,101 1,100 1,100 0))", polygonNoHoles);

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
  }

  @Test
  public void testParseMultiPolygon() throws ParseException {
    Shape p1 = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(101, 0)
        .point(101, 1)
        .point(100, 1)
        .point(100, 0)
        .build();
    Shape p2 = new PolygonBuilder(ctx)
        .point(100, 0)
        .point(102, 0)//2
        .point(102, 1)//2
        .point(100, 1)
        .point(100, 0)
        .build();
    Shape s = ctx.makeCollection(
        Arrays.asList(p1, p2)
    );
    assertParses("MULTIPOLYGON(" +
        "((100 0, 101 0, 101 1, 100 1, 100 0))" + ',' +
        "((100 0, 102 0, 102 1, 100 1, 100 0))" +
        ")", s);
  }

}
