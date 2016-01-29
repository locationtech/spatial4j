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

package org.locationtech.spatial4j.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GeneralWktTest extends GeneralReadWriteShapeTest {

  ShapeReader reader;
  ShapeWriter writer;

  ShapeWriter writerForTests;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    
    reader = ctx.getFormats().getReader(ShapeIO.WKT);
    writer = ctx.getFormats().getWriter(ShapeIO.WKT);
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

  //TODO: Either the WKT read/writer should try to flatten to the original type (not GeometryCollection)
  //  and/or ShapeCollection needs to become typed.

  @Ignore @Test @Override
  public void testWriteThenReadMultiPoint() throws Exception {
    super.testWriteThenReadMultiPoint();
  }

  @Ignore @Test @Override
  public void testWriteThenReadMultiLineString() throws Exception {
    super.testWriteThenReadMultiLineString();
  }

  @Ignore @Test @Override
  public void testWriteThenReadMultiPolygon() throws Exception {
    super.testWriteThenReadMultiPolygon();
  }
}