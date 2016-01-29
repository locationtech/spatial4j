/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

public class BinaryCodecTest extends BaseRoundTripTest<SpatialContext> {

  @Override
  public SpatialContext initContext() {
    return SpatialContext.GEO;
  }

  @Test
  public void testRect() throws Exception {
    assertRoundTrip(wkt("ENVELOPE(-10, 180, 42.3, 0)"));
  }

  @Test
  public void testCircle() throws Exception {
    assertRoundTrip(wkt("BUFFER(POINT(-10 30), 5.2)"));
  }

  @Test
  public void testCollection() throws Exception {
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
  protected void assertRoundTrip(Shape shape, boolean andEquals) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    binaryCodec.writeShape(new DataOutputStream(baos), shape);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    assertEquals(shape, binaryCodec.readShape(new DataInputStream(bais)));
  }

}
