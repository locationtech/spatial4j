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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import io.jeo.geom.GeomBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.io.IOException;
import java.text.ParseException;

public abstract class GeneralReadWriteShapeTest extends BaseRoundTripTest<JtsSpatialContext> {

  protected GeomBuilder gb;

  @Before
  public void setUp() {
    gb = new GeomBuilder();
  }

  @Override
  public JtsSpatialContext initContext() {
    // Like the out-of-the-box GEO, but it wraps datelines
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.geo = true;
    factory.normWrapLongitude = true;
    return new JtsSpatialContext(factory);
  }
  
  
  protected abstract ShapeReader getShapeReader();
  protected abstract ShapeWriter getShapeWriter();

  protected abstract ShapeWriter getShapeWriterForTests();
  
  @Override
  protected void assertRoundTrip(Shape shape, boolean andEquals) {
    try {
      String str = getShapeWriter().toString(shape);
      Shape out = getShapeReader().read(str);

      // GeoJSON has limited numberic precision so the off by .0000001 does not affect its equals
      ShapeWriter writer = getShapeWriterForTests();
      Assert.assertEquals(writer.toString(shape), writer.toString(out));
      
      if(andEquals) {
        Assert.assertEquals(shape, out);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InvalidShapeException e) {
      throw new RuntimeException(e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Test
  public void testWriteThenReadPoint() throws Exception {
    assertRoundTrip(point());
  }

  @Test
  public void testWriteThenReadLineString() throws Exception {
    assertRoundTrip(line());
  }

  @Test
  public void testWriteThenReadPolygon() throws Exception {
    assertRoundTrip(polygon1());
    assertRoundTrip(polygon2());
  }

  @Test
  public void testWriteThenReadMultiPoint() throws Exception {
    assertRoundTrip(multiPoint());
  }

  @Test
  public void testWriteThenReadMultiLineString() throws Exception {
    assertRoundTrip(multiLine());
  }

  @Test
  public void testWriteThenReadMultiPolygon() throws Exception {
    assertRoundTrip(multiPolygon());
  }

  @Test
  public void testWriteThenReadRectangle() throws Exception {
    assertRoundTrip(polygon1().getBoundingBox());
  }
  
  @Test
  public void testWriteThenReadCollection() throws Exception {
    assertRoundTrip(collection());
  }

  @Test
  public void testWriteThenReadBufferedLine() throws Exception {
    assertRoundTrip(bufferedLine());
  }

  String pointText() {
    return strip("{'type': 'Point','coordinates':[100.1,0.1]}");
  }

  com.spatial4j.core.shape.Point point() {
    return ctx.makePoint(100.1, 0.1);
  }

  String lineText() {
    return strip(
      "{'type': 'LineString', 'coordinates': [[100.1,0.1],[101.1,1.1]]}");
  }

  Shape line() {
    return ctx.makeShape(gb.points(100.1, 0.1, 101.1,1.1).toLineString());
  }

  Shape polygon1() {
    // close to a rectangle but not quite
    return ctx.makeShape(gb.points(100.1, 0.1, 101.2, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring().toPolygon());
  }

  String polygonText1() {
    return strip("{ 'type': 'Polygon',"+
    "'coordinates': ["+
    "  [ [100.1, 0.1], [101.2, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1] ]"+
    "  ]"+
     "}");
  }

  String polygonText2() {
    return strip("{ 'type': 'Polygon',"+
    "  'coordinates': ["+
    "    [ [100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1] ],"+
    "    [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]"+
    "    ]"+
    "   }");
  }

  Shape polygon2() {
    return ctx.makeShape(gb.points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring()
      .points(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2).ring().toPolygon());
  }

  String multiPointText() {
    return strip(
      "{ 'type': 'MultiPoint',"+
        "'coordinates': [ [100.1, 0.1], [101.1, 1.1] ]"+
      "}");
  }

  Shape multiPoint() {
    return ctx.makeShape( gb.points(100.1, 0.1, 101.1, 1.1).toMultiPoint() );
  }
  

  String multiLineText() {
    return strip(
      "{ 'type': 'MultiLineString',"+
      "  'coordinates': ["+
      "    [ [100.1, 0.1], [101.1, 1.1] ],"+
      "    [ [102.1, 2.1], [103.1, 3.1] ]"+
      "    ]"+
      "  }");
  }

  Shape multiLine() {
    return ctx.makeShape(gb.points(100.1, 0.1, 101.1, 1.1).lineString()
      .points(102.1, 2.1, 103.1, 3.1).lineString().toMultiLineString());
  }
  
  String multiPolygonText() {
    return strip(
    "{ 'type': 'MultiPolygon',"+
    "  'coordinates': ["+
    "    [[[102.1, 2.1], [103.1, 2.1], [103.1, 3.1], [102.1, 3.1], [102.1, 2.1]]],"+// rect
    "    [[[100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1]],"+ // rect with rect hole
    "     [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]"+
    "    ]"+
    "  }");
  }

  Shape multiPolygon() {
    return ctx.makeShape(
            gb.points(102.1, 2.1, 103.1, 2.1, 103.1, 3.1, 102.1, 3.1, 102.1, 2.1).ring().polygon()
                    .points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring()
                    .points(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2).ring().polygon()
                    .toMultiPolygon());
  }

  Rectangle rectangle() {
    return ctx.makeRectangle(100.1, 101.1, 0.1, 1.1);
  }

  String rectangleText() {
    return strip(
      "{" +
      "'type':'Polygon'," +
      "'coordinates': [[[100.1,0.1], [100.1,1.1], [101.1,1.1], [101.1,0.1], [100.1,0.1]]]" +
      "}");
  }

  String collectionText() {
    return strip(
      "{ 'type': 'GeometryCollection',"+
      "  'geometries': ["+
      "    { 'type': 'Point',"+
      "    'coordinates': [100.1, 0.1]"+
      "    },"+
      "    { 'type': 'LineString',"+
      "    'coordinates': [ [101.1, 0.1], [102.1, 1.1] ]"+
      "    }"+
      "  ]"+
      "  }");
  }

  Shape collection() {
    return ctx.makeShapeFromGeometry(gb.point(100.1, 0.1).point().points(101.1, 0.1, 102.1, 1.1).lineString().toCollection());
  }

  String bufferedLineText() {
    return strip(
        "{'type': 'LineString', 'coordinates': [[100.1,0.1],[101.1,1.1]], 'buffer': 10}");
  }

  Shape bufferedLine() {
    return ctx.makeBufferedLineString(Arrays.asList(ctx.makePoint(100.1, 0.1),
        ctx.makePoint(101.1, 1.1)), 10);
  }

  String strip(String json) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == ' ' || c == '\n') continue;
      if (c == '\'') {
        sb.append("\"");
      }
      else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}