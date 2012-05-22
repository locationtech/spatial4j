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

package com.spatial4j.core.context;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.CircleImpl;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;


@SuppressWarnings("unchecked")
public class SpatialContextTest extends RandomizedTest {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
            $(SpatialContext.GEO_KM),
            $(JtsSpatialContext.GEO_KM)
    ));
  }

  private final SpatialContext ctx;

  public SpatialContextTest(SpatialContext ctx) {
    this.ctx = ctx;
  }


  private <T extends Shape> T writeThenRead( T s ) throws IOException {
    String buff = ctx.toString( s );
    return (T) ctx.readShape( buff );
  }

  @Test
  public void testSimpleShapeIO() throws Exception {
    // Simple Point
    Shape s = ctx.readShape("10 20");
    assertEquals(s,writeThenRead(s));
    assertEquals(s,ctx.readShape("20,10"));//check comma for y,x format
    assertEquals(s,ctx.readShape("20, 10"));//test space
    Point p = (Point) s;
    assertEquals(10.0, p.getX(), 0D);
    assertEquals(20.0, p.getY(), 0D);
    assertFalse(s.hasArea());

    // BBOX
    s = ctx.readShape("-10 -20 10 20");
    assertEquals(s,writeThenRead(s));
    Rectangle b = (Rectangle) s;
    assertEquals(-10.0, b.getMinX(), 0D);
    assertEquals(-20.0, b.getMinY(), 0D);
    assertEquals(10.0, b.getMaxX(), 0D);
    assertEquals(20.0, b.getMaxY(), 0D);
    assertTrue(s.hasArea());

    // Circle
    s = ctx.readShape("Circle(1.23 4.56 distance=7.89)");
    assertEquals(s,writeThenRead(s));
    CircleImpl circle = (CircleImpl)s;
    assertEquals(1.23, circle.getCenter().getX(), 0D);
    assertEquals(4.56, circle.getCenter().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
    assertTrue(s.hasArea());

    Shape s2 = ctx.readShape("Circle( 4.56,1.23 d=7.89 )"); // use lat,lon and use 'd' abbreviation
    assertEquals(s,s2);
  }


  //  Looking for more tests?  Shapes are tested in TestShapes2D.

}
