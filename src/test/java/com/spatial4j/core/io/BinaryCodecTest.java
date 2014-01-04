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
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;

public class BinaryCodecTest extends RandomizedTest {

  final SpatialContext ctx;
  private ByteBuffer byteBuf = ByteBuffer.allocate(10*1024);//plenty for our tests
  private BinaryCodec binaryCodec;

  protected BinaryCodecTest(SpatialContext ctx) {
    this.ctx = ctx;
    binaryCodec = ctx.getBinaryCodec();//stateless
  }

  public BinaryCodecTest() {
    this(SpatialContext.GEO);
  }

  @Before
  public void setUp() {
    byteBuf.clear();
  }

  //This test uses WKT to specify the shapes because the Jts based subclass tests will test
  // using floats instead of doubles, and WKT is normalized whereas ctx.makeXXX is not.

  @Test
  public void testPoint() {
    byteBuf.limit(1+8+8);
    assertRoundTrip(wkt("POINT(-10 80.3)"));
  }

  @Test
  public void testRect() {
    byteBuf.limit(1+8+8+8+8);
    assertRoundTrip(wkt("ENVELOPE(-10, 180, 42.3, 0)"));
  }

  @Test
  public void testCircle() {
    byteBuf.limit(1+8+8+8);
    assertRoundTrip(wkt("BUFFER(POINT(-10 30), 5.2)"));
  }

  @Test
  public void testCollection() {
    ShapeCollection s = ctx.makeCollection(
        Arrays.asList(
            randomShape(),
            randomShape(),
            randomShape()
        )
    );
    assertRoundTrip(s);
  }

  protected Shape wkt(String wkt) {
    try {
      return ctx.readShapeFromWkt(wkt);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  protected Shape randomShape() {
    switch (randomInt(2)) {//inclusive
      case 0: return wkt("POINT(-10 80.3)");
      case 1: return wkt("ENVELOPE(-10, 180, 42.3, 0)");
      case 2: return wkt("BUFFER(POINT(-10 30), 5.2)");
      default: throw new Error();
    }
  }

  protected void assertRoundTrip(Shape shape) {
    binaryCodec.writeShape(byteBuf, shape);
    byteBuf.flip();
    assertEquals(shape, binaryCodec.readShape(byteBuf));
  }
}
