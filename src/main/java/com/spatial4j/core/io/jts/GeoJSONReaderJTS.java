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

package com.spatial4j.core.io.jts;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.noggit.JSONParser;

import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.io.GeoJSONReader;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

public class GeoJSONReaderJTS extends GeoJSONReader {

  protected final JtsSpatialContext ctx;

  public GeoJSONReaderJTS(JtsSpatialContext ctx, SpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
  }

  // --------------------------------------------------------------
  // Read GeoJSON
  // --------------------------------------------------------------

  public Coordinate readCoord(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    Coordinate coord = new Coordinate();
    int idx = 0;

    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          coord.setOrdinate(idx++, parser.getDouble());
          break;

        case JSONParser.ARRAY_END:
          return coord;

        case JSONParser.STRING:
        case JSONParser.BOOLEAN:
        case JSONParser.NULL:
        case JSONParser.OBJECT_START:
        case JSONParser.OBJECT_END:
        case JSONParser.ARRAY_START:
        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return coord;
  }

  public List<Coordinate> readCoordList(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    List<Coordinate> coords = new ArrayList<Coordinate>();
    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          coords.add(readCoord(parser));
          break;

        case JSONParser.ARRAY_END:
          return coords;

        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return coords;
  }

  @Override
  protected Shape readPoint(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    Coordinate coord = readCoord(parser);
    return ctx.makePoint(coord.x, coord.y);
  }

  @Override
  protected Shape readLineString(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    List<Coordinate> coords = readCoordList(parser);

    GeometryFactory factory = ctx.getGeometryFactory();
    CoordinateSequence seq =
        factory.getCoordinateSequenceFactory()
            .create(coords.toArray(new Coordinate[coords.size()]));
    LineString geo = ctx.getGeometryFactory().createLineString(seq);
    return ctx.makeShape(geo);
  }

  @Override
  protected Shape readPolygon(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    GeometryFactory gf = ctx.getGeometryFactory();
    return ctx.makeShape(createPolygon(gf, readCoordinates(parser)));
  }

  @Override
  protected Shape makeShapeFromCoords(String type, List coords) {
    GeometryFactory gf = ctx.getGeometryFactory();

    if ("Polygon".equals(type)) {
      return ctx.makeShape(createPolygon(gf, coords));
    }
    if ("MultiPoint".equals(type)) {
      return ctx.makeShape(createMultiPoint(gf, coords));
    }
    if ("MultiLineString".equals(type)) {
      return ctx.makeShape(createMultiLineString(gf, coords));
    }
    if ("MultiPolygon".equals(type)) {
      return ctx.makeShape(createMultiPolygon(gf, coords));
    }
    return null;
  }


  // --------------------------------------------------------------
  // FROM JEO.ORG:
  // https://github.com/jeo/jeo/blob/master/core/src/main/java/org/jeo/geojson/parser/GeometryHandler.java#L111
  // --------------------------------------------------------------

  Point createPoint(GeometryFactory gf, List list) {
    return gf.createPoint(coord(list));
  }

  LineString createLineString(GeometryFactory gf, List list) {
    return gf.createLineString(coordseq(list));
  }

  Polygon createPolygon(GeometryFactory gf, List list) {
    LinearRing shell = gf.createLinearRing(coordseq((List) ensureSize(list, 1).get(0)));
    LinearRing[] holes = list.size() > 1 ? new LinearRing[list.size() - 1] : null;

    for (int i = 1; i < list.size(); i++) {
      holes[i - 1] = gf.createLinearRing(coordseq((List) list.get(i)));
    }
    return gf.createPolygon(shell, holes);
  }

  MultiPoint createMultiPoint(GeometryFactory gf, List list) {
    return gf.createMultiPoint(coordseq(list));
  }

  MultiLineString createMultiLineString(GeometryFactory gf, List list) {
    LineString[] lines = new LineString[ensureSize(list, 1).size()];
    for (int i = 0; i < list.size(); i++) {
      lines[i] = createLineString(gf, (List) list.get(i));
    }
    return gf.createMultiLineString(lines);
  }

  MultiPolygon createMultiPolygon(GeometryFactory gf, List list) {
    Polygon[] polys = new Polygon[ensureSize(list, 1).size()];
    for (int i = 0; i < list.size(); i++) {
      polys[i] = createPolygon(gf, (List) list.get(i));
    }
    return gf.createMultiPolygon(polys);
  }

  GeometryCollection createGeometryCollection(GeometryFactory gf, List geoms) {
    return gf.createGeometryCollection((Geometry[]) geoms.toArray(new Geometry[geoms.size()]));
  }

  Coordinate coord(List list) {
    ensureSize(list, 2);

    double x = number(list.get(0));
    double y = number(list.get(1));
    double z = list.size() > 2 ? number(list.get(2)) : Double.NaN;

    Coordinate c = new Coordinate(x, y);
    if (!Double.isNaN(z)) {
      c.z = z;
    }
    return c;
  }

  CoordinateSequence coordseq(List list) {
    ensureSize(list, 1);

    int dim = ensureSize((List) list.get(0), 2).size();

    CoordinateSequence seq =
        PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(list.size(), dim);

    for (int i = 0; i < list.size(); i++) {
      List c = (List) list.get(i);
      seq.setOrdinate(i, 0, number(c.get(0)));
      seq.setOrdinate(i, 1, number(c.get(1)));

      if (dim > 2) {
        seq.setOrdinate(i, 2, number(c.get(2)));
      }
    }

    return seq;
  }

  double number(Object obj) {
    return ((Number) obj).doubleValue();
  }

  List ensureSize(List list, int size) {
    if (list.size() < size) {
      throw new IllegalArgumentException(String.format(Locale.ROOT,
          "expected coordinate arary of size %d but is of size %d", size, list.size()));
    }
    return list;
  }
}
