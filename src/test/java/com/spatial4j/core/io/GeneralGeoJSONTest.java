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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class GeneralGeoJSONTest extends GeneralReadWriteShapeTest {

  ShapeReader reader;
  ShapeWriter writer;

  ShapeWriter writerForTests;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    
    reader = ctx.getFormats().getReader(ShapeIO.GeoJSON);
    writer = ctx.getFormats().getWriter(ShapeIO.GeoJSON);
    writerForTests = writer; //ctx.getFormats().getWriter(ShapeIO.GeoJSON);

    Assert.assertNotNull(reader);
    Assert.assertNotNull(writer);
    Assert.assertNotNull(writerForTests);
  }

  @Override
  protected ShapeReader getShapeReader() {
    return reader;
  }

  @Override
  protected ShapeWriter getShapeWriter() {
    return writer;
  }

  @Override
  protected ShapeWriter getShapeWriterForTests() {
    return writerForTests;
  }
  

  @Test
  public void testCircle() {
    assertRoundTrip(wkt("BUFFER(POINT(-10 30), 40)"), false);
  }
}