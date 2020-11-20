/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
    ShapeCollection<Shape> s = ctx.makeCollection(
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
