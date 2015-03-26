/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.spatial4j.core.GeomBuilder;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

public class GeoJSONReadWriteTest {

  ShapeFormat format;
  GeometryFactory gf;
  GeomBuilder gb;
  JtsSpatialContext ctx;

  @Before
  public void setUp() {
    ctx = JtsSpatialContext.GEO;
    format = ctx.getFormat(GeoJSONFormat.FORMAT);
    gb = new GeomBuilder();
  }

  @Test
  public void testParsePoint() throws Exception {
    Shape v = format.read(pointText(), true);
    assertTrue(point().equals(v));
  }

  @Test
  public void testEncodePoint() throws Exception {
    assertEquals(pointText(), format.toString(point()));
  }

  @Test
  public void testParseLineString() throws Exception {
    assertTrue(line().equals(format.read(lineText(), true)));
  }

  @Test
  public void testEncodeLineString() throws Exception {
    assertEquals(lineText(), format.toString(line()));
  }

  @Test
  public void testParsePolygon() throws Exception {
    assertTrue(polygon1().equals(format.read(polygonText1(),true)));
    assertTrue(polygon2().equals(format.read(polygonText2(),true)));
  }

  @Test
  public void testEncodePolygon() throws Exception {
    assertEquals(polygonText1(), format.toString(polygon1()));
    assertEquals(polygonText2(), format.toString(polygon2()));
  }

  @Test
  public void testParseMultiPoint() throws Exception {
    assertTrue(multiPoint().equals(format.read(multiPointText(),true)));
  }

  @Test
  public void testEncodeMultiPoint() throws Exception {
    assertEquals(multiPointText(), format.toString(multiPoint()));
  }

  @Test
  public void testParseMultiLineString() throws Exception {
    assertTrue(multiLine().equals(format.read(multiLineText(),true)));
  }

  @Test
  public void testEncodeMultiLineString() throws Exception {
    assertEquals(multiLineText(), format.toString(multiLine()));
  }

  @Test
  public void testParseMultiPolygon() throws Exception {
    assertTrue(multiPolygon().equals(format.read(multiPolygonText(),true)));
  }

  @Test
  public void testEncodeMultiPolygon() throws Exception {
    assertEquals(multiPolygonText(), format.toString(multiPolygon()));
  }

//  @Test
//  public void testParseGeometryCollection() throws Exception {
//    assertEquals(collection(), format.read(collectionText(),true));
//  }
//
//  @Test
//  public void testEncodeGeometryCollection() throws Exception {
//    assertEquals(collectionText(), format.toString(collection()));
//  }

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
    return ctx.makeShape(gb.points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring().toPolygon());
  }

  String polygonText1() {
    return strip("{ 'type': 'Polygon',"+
    "'coordinates': ["+
    "  [ [100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1] ]"+
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
    "    [[[102.1, 2.1], [103.1, 2.1], [103.1, 3.1], [102.1, 3.1], [102.1, 2.1]]],"+
    "    [[[100.1, 0.1], [101.1, 0.1], [101.1, 1.1], [100.1, 1.1], [100.1, 0.1]],"+
    "     [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]"+
    "    ]"+
    "  }");
  }

  Shape multiPolygon() {
    return ctx.makeShape(gb.points(102.1, 2.1,103.1, 2.1,103.1, 3.1,102.1, 3.1,102.1, 2.1).ring().polygon()
      .points(100.1, 0.1, 101.1, 0.1, 101.1, 1.1, 100.1, 1.1, 100.1, 0.1).ring()
      .points(100.2, 0.2, 100.8, 0.2, 100.8, 0.8, 100.2, 0.8, 100.2, 0.2).ring().polygon()
      .toMultiPolygon());
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
    return ctx.makeShape(gb.point(100.1,0.1).point().points(101.1, 0.1, 102.1, 1.1).lineString().toCollection());
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
