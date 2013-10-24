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


import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * An extensible parser for <a href="http://en.wikipedia.org/wiki/Well-known_text">
 * Well Known Text (WKT)</a>.
 * The shapes supported by this class are:
 * <ul>
 *   <li>POINT</li>
 *   <li>ENVELOPE</li>
 *   <li>GEOMETRYCOLLECTION</li>
 * </ul>
 * <p />
 * To support more shapes, extend this class and override {@link #parseShapeByType(String)}.
 * <p />
 * Note, instances are not threadsafe but are reusable.
 */
public class WKTShapeParser {

  //TODO
  // * EMPTY shapes  (new EmptyShape with name?)
  // * shape: multipoint (both syntax's)

  /** Set in {@link #parseIfSupported(String)}. */
  protected String rawString;

  /** Offset of the next char in {@link #rawString} to be read. */
  protected int offset;

  protected final SpatialContext ctx;

  public WKTShapeParser(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public SpatialContext getCtx() {
    return ctx;
  }

  /**
   * Parses the wktString, returning the defined Shape.
   *
   * @return Non-null Shape defined in the String
   * @throws ParseException Thrown if there is an error in the Shape definition
   */
  public Shape parse(String wktString)  throws ParseException {
    Shape shape = parseIfSupported(wktString);//sets rawString & offset
    if (shape != null)
      return shape;
    String shortenedString = (wktString.length() <= 128 ? wktString : wktString.substring(0, 128-3)+"...");
    throw new ParseException("Unknown Shape definition [" + shortenedString + "]", offset);
  }

  /**
   * Parses the wktString, returning the defined Shape. If it can't because the
   * shape name is unknown or an empty or blank string was passed, then it returns null.
   * If the WKT starts with a supported shape but contains an inner unsupported shape then
   * it will result in a {@link ParseException}.
   *
   * @param wktString non-null, can be empty or have surrounding whitespace
   * @return Shape, null if unknown / unsupported shape.
   * @throws ParseException Thrown if there is an error in the Shape definition.
   */
  public Shape parseIfSupported(String wktString) throws ParseException {
    this.rawString = wktString;
    this.offset = 0;
    consumeWhitespace();//leading
    if (offset >= rawString.length())
      return null;
    if (!Character.isLetter(wktString.charAt(offset)))//optimization short-circuit
      return null;

    String shapeType = nextWord();
    Shape result = parseShapeByType(shapeType);
    if (result != null) {
      if (offset != wktString.length())
        throw new ParseException("end of shape expected", offset);
    }
    return result;
  }

  /**
   * Parses the remainder of a shape definition following the shape's name
   * given as {@code shapeType} already consumed via
   * {@link #nextWord()}. If
   * it's able to parse the shape, {@link #offset} should be advanced beyond
   * it (e.g. to the ',' or ')' or EOF in general). The default implementation
   * checks the name against some predefined names and calls corresponding
   * parse methods to handle the rest. Overriding this method is an
   * excellent extension point for additional shape types.
   *
   * @param shapeType Non-Null string; could have mixed case. The first character is a letter.
   * @return The shape or null if not supported / unknown.
   * @throws ParseException
   */
  protected Shape parseShapeByType(String shapeType) throws ParseException {
    if (shapeType.equalsIgnoreCase("POINT")) {
      return parsePointShape();
    } else if (shapeType.equalsIgnoreCase("ENVELOPE")) {
      return parseEnvelopeShape();
    } else if (shapeType.equalsIgnoreCase("GEOMETRYCOLLECTION")) {
      return parseGeometryCollectionShape();
    }
    assert Character.isLetter(shapeType.charAt(0)) : "Shape must start with letter: "+shapeType;
    return null;
  }

  /**
   * Parses a Point Shape from the raw String.
   * <p />
   * Point: 'POINT' '(' coordinate ')'
   *
   * @return Point Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Point correctly
   */
  protected Shape parsePointShape() throws ParseException {
    expect('(');
    Point coordinate = point();
    expect(')');
    return coordinate;
  }

  /**
   * Parses an Envelope (Rectangle) Shape from the raw String.
   * <p />
   * Source: OGC "Catalogue Services Specification", the "CQL" (Common Query Language) sub-spec.
   * <p />
   * Envelope: 'ENVELOPE' '(' x1 ',' x2 ',' y2 ',' y1 ')'
   *
   * @return Envelope Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Envelope correctly
   */
  protected Shape parseEnvelopeShape() throws ParseException {
    expect('(');
    double x1 = nextDouble();
    expect(',');
    double x2 = nextDouble();
    expect(',');
    double y2 = nextDouble();
    expect(',');
    double y1 = nextDouble();
    expect(')');
    return ctx.makeRectangle(x1, x2, y1, y2);
  }

  /**
   * Reads a ShapeCollection (AKA GeometryCollection) from the raw string.
   * <p />
   * GeometryCollection: '(' shape (',' shape )* ')'
   *
   * @throws ParseException
   */
  protected Shape parseGeometryCollectionShape() throws ParseException {
    List<Shape> shapes = new ArrayList<Shape>();
    expect('(');
    do {
      Shape shape = shape();
      shapes.add(shape);
    } while (consumeIfAt(','));
    expect(')');
    return ctx.makeCollection(shapes);
  }

  /** Reads a shape from the current position. */
  protected Shape shape() throws ParseException {
    String type = nextWord();
    Shape shape = parseShapeByType(type);
    if (shape == null)
      throw new ParseException("Shape of type "+type+" is unknown", offset);
    return shape;
  }

  /**
   * Reads a list of Points (AKA CoordinateSequence) from the current position.
   * <p />
   * CoordinateSequence: '(' coordinate (',' coordinate )* ')'
   *
   * @return Points read from the current position. Non-null, non-empty.
   * @throws ParseException Thrown if reading the CoordinateSequence is unsuccessful
   */
  protected List<Point> pointList() throws ParseException {
    List<Point> sequence = new ArrayList<Point>();
    expect('(');
    do {
      sequence.add(point());
    } while (consumeIfAt(','));
    expect(')');
    return sequence;
  }

  /**
   * Reads a raw Point (AKA Coordinate) from the current position.
   * <p />
   * Coordinate: number number
   *
   * @return The point read from the current position.
   * @throws ParseException Thrown if reading the Coordinate is unsuccessful
   */
  protected Point point() throws ParseException {
    double x = nextDouble();
    double y = nextDouble();
    return ctx.makePoint(x, y);
  }

  /**
   * Reads the word starting at the current character position. The word
   * terminates once {@link Character#isJavaIdentifierPart(char)} returns false (or EOF).
   * {@link #offset} is advanced past whitespace.
   *
   * @return Non-null non-empty String.
   * @throws ParseException if the word would otherwise be empty.
   */
  protected String nextWord() throws ParseException {
    int startOffset = offset;
    while (offset < rawString.length() &&
        Character.isJavaIdentifierPart(rawString.charAt(offset))) {
      offset++;
    }
    if (startOffset == offset)
      throw new ParseException("Word expected", startOffset);
    String result = rawString.substring(startOffset, offset);
    consumeWhitespace();
    return result;
  }

  /**
   * Reads in a double from the String. Parses digits with an optional decimal, sign, or exponent.
   * NaN and Infinity are not supported.
   * {@link #offset} is advanced past whitespace.
   *
   * @return Double value
   * @throws ParseException Thrown if the String is exhausted before the number is delimited
   */
  protected double nextDouble() throws ParseException {
    int startOffset = offset;
    for (; offset < rawString.length(); offset++ ) {
      char c = rawString.charAt(offset);
      if (!(Character.isDigit(c) || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E')) {
        break;
      }
    }
    if (startOffset == offset)
      throw new ParseException("Expected a number", offset);
    double result;
    try {
      result = Double.parseDouble(rawString.substring(startOffset, offset));
    } catch (Exception e) {
      throw new ParseException(e.toString(), offset);
    }
    consumeWhitespace();
    return result;
  }

  /**
   * Verifies that the current character is of the expected value.
   * If the character is the expected value, then it is consumed and
   * {@link #offset} is advanced past whitespace.
   *
   * @param expected Value that the next non-whitespace character should be
   * @throws ParseException Thrown if the next non-whitespace character is not
   *         the expected value
   */
  protected void expect(char expected) throws ParseException {
    if (offset >= rawString.length())
      throw new ParseException("Expected [" + expected + "] found EOF", offset);
    char c = rawString.charAt(offset);
    if (c != expected)
      throw new ParseException("Expected [" + expected + "] found [" + c + "]", offset);
    offset++;
    consumeWhitespace();
  }

  /**
   * If the current character is {@code expected}, then offset is advanced after it and any
   * subsequent whitespace.
   *
   * @param expected The expected char.
   * @return true if consumed
   */
  protected boolean consumeIfAt(char expected) {
    if (offset < rawString.length() && rawString.charAt(offset) == expected) {
      offset++;
      consumeWhitespace();
      return true;
    }
    return false;
  }

  /**
   * Moves offset to next non-whitespace character. Doesn't move if the offset is already at
   * non-whitespace.
   */
  protected void consumeWhitespace() {
    for (; offset < rawString.length(); offset++) {
      if (!Character.isWhitespace(rawString.charAt(offset))) {
        return;
      }
    }
  }

  /**
   * Returns the next chunk of text till the next ',' or ')' (non-inclusive)
   * or EOF. If a '(' is encountered, then it looks past its matching ')',
   * taking care to handle nested matching parenthesis too. It's designed to be
   * of use to subclasses that wish to get the entire subshape at the current
   * position as a string so that it might be passed to other software that
   * will parse it.
   * <p/>
   * Example:
   * <pre>
   *   OUTER(INNER(3, 5))
   * </pre>
   * If this is called when offset is at the first character, then it will
   * return this whole string.  If called at the "I" then it will return
   * "INNER(3, 5)".  If called at "3", then it will return "3".  In all cases,
   * offset will be positioned at the next position following the returned
   * substring.
   *
   * @return non-null substring.
   */
  protected String nextSubShapeString() throws ParseException {
    int startOffset = offset;
    int parenStack = 0;//how many parenthesis levels are we in?
    for (; offset < rawString.length(); offset++) {
      char c = rawString.charAt(offset);
      if (c == ',') {
        if (parenStack == 0)
          break;
      } else if (c == ')') {
        if (parenStack == 0)
          break;
        parenStack--;
      } else if (c == '(') {
        parenStack++;
      }
    }
    if (parenStack != 0)
      throw new ParseException("Unbalanced parenthesis", startOffset);
    return rawString.substring(startOffset, offset);
  }
}
