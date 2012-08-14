/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;


@SuppressWarnings("unchecked")
public class ShapeReadWriterTest extends RandomizedTest {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
            $(SpatialContext.GEO),
            $(JtsSpatialContext.GEO)
    ));
  }

  private final SpatialContext ctx;

  public ShapeReadWriterTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  private <T extends Shape> T writeThenRead( T s ) throws IOException {
    String buff = ctx.toString( s );
    return (T) ctx.readShape( buff );
  }

  @Test
  public void testPoint() throws IOException {
    Shape s = ctx.readShape("10 20");
    assertEquals(ctx.makePoint(10,20),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,ctx.readShape("20,10"));//check comma for y,x format
    assertEquals(s,ctx.readShape("20, 10"));//test space
    assertFalse(s.hasArea());
  }

  @Test
  public void testRectangle() throws IOException {
    Shape s = ctx.readShape("-10 -20 10 20");
    assertEquals(ctx.makeRectangle(-10, 10, -20, 20),s);
    assertEquals(s,writeThenRead(s));
    assertTrue(s.hasArea());
  }

  @Test
  public void testCircle() throws IOException {
    Shape s = ctx.readShape("Circle(1.23 4.56 distance=7.89)");
    assertEquals(ctx.makeCircle(1.23, 4.56, 7.89),s);
    assertEquals(s,writeThenRead(s));
    assertEquals(s,ctx.readShape("CIRCLE( 4.56,1.23 d=7.89 )")); // use lat,lon and use 'd' abbreviation
    assertTrue(s.hasArea());
  }


  //  Looking for more tests?  Shapes are tested in TestShapes2D.

}
