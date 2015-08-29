/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;

public class BinaryCodecTest extends BaseRoundTripTest<SpatialContext> {

  @Override
  public SpatialContext initContext() {
    return SpatialContext.GEO;
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

  @Override
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
