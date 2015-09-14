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

import com.spatial4j.core.shape.Shape;
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

  //
  // Below ported from GeoJSONReadWriteTest:
  //

  @Test
  public void testParsePoint() throws Exception {
    Shape v = reader.read(pointText());
    assertTrue(point().equals(v));
  }

  @Test
  public void testEncodePoint() throws Exception {
    assertEquals(pointText(), writer.toString(point()));
  }

  @Test
  public void testParseLineString() throws Exception {
    assertTrue(line().equals(reader.read(lineText())));
  }

  @Test
  public void testEncodeLineString() throws Exception {
    assertEquals(lineText(), writer.toString(line()));
  }

  @Test
  public void testParsePolygon() throws Exception {
    assertTrue(polygon1().equals(reader.read(polygonText1())));
    assertTrue(polygon2().equals(reader.read(polygonText2())));
  }

  @Test
  public void testEncodePolygon() throws Exception {
    assertEquals(polygonText1(), writer.toString(polygon1()));
    assertEquals(polygonText2(), writer.toString(polygon2()));
  }

  @Test
  public void testParseMultiPoint() throws Exception {
    assertTrue(multiPoint().equals(reader.read(multiPointText())));
  }

  @Test
  public void testEncodeMultiPoint() throws Exception {
    assertEquals(multiPointText(), writer.toString(multiPoint()));
  }

  @Test
  public void testParseMultiLineString() throws Exception {
    assertTrue(multiLine().equals(reader.read(multiLineText())));
  }

  @Test
  public void testEncodeMultiLineString() throws Exception {
    assertEquals(multiLineText(), writer.toString(multiLine()));
  }

  @Test
  public void testParseMultiPolygon() throws Exception {
    assertTrue(multiPolygon().equals(reader.read(multiPolygonText())));
  }

  @Test
  public void testEncodeMultiPolygon() throws Exception {
    assertEquals(multiPolygonText(), writer.toString(multiPolygon()));
  }

  @Test
  public void testEncodeRectangle() throws Exception {
    assertEquals(rectangleText(), strip(writer.toString(rectangle())));
  }

  @Test
  public void testParseGeometryCollection() throws Exception {
    assertEquals(collection(), reader.read(collectionText()));
  }

  @Test
  public void testEncodeGeometryCollection() throws Exception {
    assertEquals(collectionText(), strip(writer.toString(collection())));
  }

  @Test
  public void testEncodeBufferedLineString() throws Exception {
    assertEquals(bufferedLineText(), strip(writer.toString(bufferedLine())));
  }

}