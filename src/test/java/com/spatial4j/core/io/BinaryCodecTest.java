/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

public class BinaryCodecTest extends RandomizedTest {

  final SpatialContext ctx;
  private BinaryCodec binaryCodec;

  protected BinaryCodecTest(SpatialContext ctx) {
    this.ctx = ctx;
    binaryCodec = ctx.getBinaryCodec();//stateless
  }

  public BinaryCodecTest() {
    this(SpatialContext.GEO);
  }

  //This test uses WKT to specify the shapes because the Jts based subclass tests will test
  // using floats instead of doubles, and WKT is normalized whereas ctx.makeXXX is not.

  @Test
  public void testPoint() {
    assertRoundTrip(wkt("POINT(-10 80.3)"));
  }

  @Test
  public void testRect() {
    assertRoundTrip(wkt("ENVELOPE(-10, 180, 42.3, 0)"));
  }

  @Test
  public void testCircle() {
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
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      binaryCodec.writeShape(new DataOutputStream(baos), shape);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      assertEquals(shape, binaryCodec.readShape(new DataInputStream(bais)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
