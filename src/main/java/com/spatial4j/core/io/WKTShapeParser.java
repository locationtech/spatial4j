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
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parser for WKT (http://en.wikipedia.org/wiki/Well-known_text).
 *
 * Note, instances are not threadsafe but are reusable.
 */
public class WKTShapeParser {

  private String rawString;
  private int offset;
  private JtsSpatialContext ctx;

  public WKTShapeParser(JtsSpatialContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Parses the rawString, returning the defined Shape
   *
   * @return Shape defined in the String
   * @throws ParseException Thrown if there is an error in the Shape definition
   */
  public Shape parse(String wktString) throws ParseException {
    this.rawString = wktString.toLowerCase(Locale.ENGLISH).trim();
    if (rawString.startsWith("point")) {
      offset = 5;
      return parsePoint();
    } else if (rawString.startsWith("polygon")) {
      offset = 7;
      return new JtsGeometry(polygon(), ctx, true);
    } else if (rawString.startsWith("multipolygon")) {
      offset = 12;
      return parseMulitPolygon();
    } else if (rawString.startsWith("envelope")) {
      offset = 8;
      return parseEnvelope();
    }

    throw new ParseException("Unknown Shape definition [" + rawString + "]", offset);
  }

  /**
   * Parses a Point Shape from the raw String
   *
   * Point: 'POINT' '(' coordinate ')'
   *
   * @return Point Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Point correctly
   */
  private Shape parsePoint() throws ParseException {
    expect('(');
    Coordinate coordinate = coordinate();
    expect(')');
    return new PointImpl(coordinate.x, coordinate.y, ctx);
  }

  /**
   * Parses a Polygon Shape from the raw String
   *
   * Polygon: 'POLYGON' coordinateSequenceList
   *
   * @return Polygon Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Polygon correctly
   */
  private Polygon polygon() throws ParseException {
    List<Coordinate[]> coordinateSequenceList = coordinateSequenceList();

    LinearRing shell = ctx.getGeometryFactory().createLinearRing(coordinateSequenceList.get(0));

    LinearRing[] holes = null;
    if (coordinateSequenceList.size() > 1) {
      holes = new LinearRing[coordinateSequenceList.size() - 1];
      for (int i = 1; i < coordinateSequenceList.size(); i++) {
        holes[i - 1] = ctx.getGeometryFactory().createLinearRing(coordinateSequenceList.get(i));
      }
    }
    return ctx.getGeometryFactory().createPolygon(shell, holes);
  }

  /**
   * Parses a MultiPolygon Shape from the raw String
   *
   * MultiPolygon: 'MULTIPOLYGON' '(' coordinateSequenceList (',' coordinateSequenceList )* ')'
   *
   * @return MultiPolygon Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the MultiPolygon correctly
   */
  private Shape parseMulitPolygon() throws ParseException {
    List<Polygon> polygons = new ArrayList<Polygon>();

    expect('(');
    polygons.add(polygon());

    while (nextCharNoWS() == ',') {
      offset++;
      polygons.add(polygon());
    }

    expect(')');
    return new JtsGeometry(
        ctx.getGeometryFactory().createMultiPolygon(polygons.toArray(new Polygon[polygons.size()])),
        ctx, true);
  }

  /**
   * Parses an Envelope Shape from the raw String
   *
   * Envelope: 'ENVELOPE' coordinateSequence
   *
   * @return Envelope Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Envelope correctly
   */
  private Shape parseEnvelope() throws ParseException {
    Coordinate[] coordinateSequence = coordinateSequence();
    return new RectangleImpl(coordinateSequence[0].x, coordinateSequence[1].x,
        coordinateSequence[1].y, coordinateSequence[0].y, ctx);
  }

  /**
   * Reads a CoordinateSequenceList from the current position
   *
   * CoordinateSequenceList: '(' coordinateSequence (',' coordinateSequence )* ')'
   *
   * @return CoordinateSequenceList read from the current position
   * @throws ParseException Thrown if reading the CoordinateSequenceList was unsucessful
   */
  private List<Coordinate[]> coordinateSequenceList() throws ParseException {
    List<Coordinate[]> sequenceList = new ArrayList<Coordinate[]>();

    expect('(');
    sequenceList.add(coordinateSequence());

    while (nextCharNoWS() == ',') {
      offset++;
      sequenceList.add(coordinateSequence());
    }

    expect(')');
    return sequenceList;
  }

  /**
   * Reads a CoordinateSequence from the current position
   *
   * CoordinateSequence: '(' coordinate (',' coordinate )* ')'
   *
   * @return CoordinateSequence read from the current position
   * @throws ParseException Thrown if reading the CoordinateSequence is unsuccessful
   */
  private Coordinate[] coordinateSequence() throws ParseException {
    List<Coordinate> sequence = new ArrayList<Coordinate>();

    expect('(');
    sequence.add(coordinate());

    while (nextCharNoWS() == ',') {
      offset++;
      sequence.add(coordinate());
    }

    expect(')');
    return sequence.toArray(new Coordinate[sequence.size()]);
  }

  /**
   * Reads a {@link Coordinate} from the current position.
   *
   * Coordinate: number number
   *
   * @return Coordinate read from the current position
   * @throws ParseException Thrown if reading the Coordinate is unsuccessful
   */
  private Coordinate coordinate() throws ParseException {
    // TODO: We need to validate the first character in the numbers
    char c = nextCharNoWS();
    double x = number();

    c = nextCharNoWS();
    double y = number();

    return new Coordinate(x, y);
  }

  /**
   * Reads in a double from the String.  Note, this method expects that the number
   * is delimited by some character.  Therefore if the String is exhausted before
   * a delimiter is encountered, a {@link ParseException} will be thrown.
   *
   * @return Double value
   * @throws ParseException Thrown if the String is exhausted before the number is delimted
   */
  private double number() throws ParseException {
    int startOffset = offset;
    try {
      for (char c = rawString.charAt(offset); offset < rawString.length(); c = rawString.charAt(++offset)) {
        if (!(Character.isDigit(c) || c == '.' || c == '-')) {
          return Double.parseDouble(rawString.substring(startOffset, offset));
        }
      }
    } catch (Exception e) {
      throw new ParseException(e.toString(), offset);
    }

    throw new ParseException("EOF reached before delimiter for the number was found", offset);
  }

  /**
   * Verifies that the next non-whitespace character is of the expected value.
   * If the character is the expected value, then it is consumed.
   *
   * @param expected Value that the next non-whitespace character should be
   * @throws ParseException Thrown if the next non-whitespace character is not
   *         the expected value
   */
  private void expect(char expected) throws ParseException {
    char c = nextCharNoWS();
    if (c != expected) {
      throw new ParseException("Expected [" + expected + "] found [" + c + "]", offset);
    }
    offset++;
  }

  /**
   * Returns the new character in the String which isn't whitespace
   *
   * @return Next non-whitespace character
   * @throws ParseException Thrown if we reach the end of the String before reaching
   *         a non-whitespace character
   */
  private char nextCharNoWS() throws ParseException {
    while (offset < rawString.length()) {
      if (!Character.isWhitespace(rawString.charAt(offset))) {
        return rawString.charAt(offset);
      }
      offset++;
    }

    throw new ParseException("EOF reached while expecting a non-whitespace character", offset);
  }
}
