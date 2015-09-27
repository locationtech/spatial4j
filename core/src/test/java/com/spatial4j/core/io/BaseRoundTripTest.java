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
import org.junit.Test;

import java.text.ParseException;

public abstract class BaseRoundTripTest<T extends SpatialContext> extends RandomizedTest {

  protected T ctx;
  protected BinaryCodec binaryCodec;

  protected BaseRoundTripTest() {
    this.ctx = initContext();
    binaryCodec = ctx.getBinaryCodec();//stateless
  }
  
  public abstract T initContext();
  
  public boolean shouldBeEqualAfterRoundTrip() {
    return true;
  }

  //This test uses WKT to specify the shapes because the Jts based subclass tests will test
  // using floats instead of doubles, and WKT is normalized whereas ctx.makeXXX is not.

  @Test
  public void testPoint() throws Exception {
    assertRoundTrip(wkt("POINT(-10 80.3)"));
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

  protected final void assertRoundTrip(Shape shape) throws Exception {
    assertRoundTrip(shape, shouldBeEqualAfterRoundTrip()); 
  }

  protected abstract void assertRoundTrip(Shape shape, boolean andEquals) throws Exception;
}
