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
import java.util.Locale;

/**
 * An extensible parser for <a href="http://en.wikipedia.org/wiki/Well-known_text">
 *   Well Known Text (WKT)</a>.
 * <p />
 * Note, instances are not threadsafe but are reusable.
 */
public class WKTShapeParser {

  /** Lower-cased and trim()'ed; set in {@link #parseIfSupported(String)}. */
  protected String rawString;
  /** Offset of the next char in {@link #rawString} to be read. */
  protected int offset;

  protected SpatialContext ctx;

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
    Shape shape = parseIfSupported(wktString);
    if (shape != null)
      return shape;
    throw new ParseException("Unknown Shape definition [" + rawString + "]", offset);
  }

  /**
   * Parses the wktString, returning the defined Shape. If it can't because the
   * shape name is unknown, then it returns null.
   *
   * @param wktString non-null
   * @return Shape, null if unknown / unsupported type.
   * @throws ParseException Thrown if there is an error in the Shape definition.
   */
  public Shape parseIfSupported(String wktString) throws ParseException {
    wktString = wktString.trim();
    if (wktString.length() == 0 || !Character.isLetter(wktString.charAt(0)))
      return null;
    this.rawString = wktString.toLowerCase(Locale.ROOT);
    this.offset = 0;
    String shapeType = nextWord();
    return parseShapeByType(shapeType);
  }

  /**
   * Parses the remainder of a shape definition following the shape's name
   * given as {@code shapeType} already consumed via
   * {@link #nextWord()}. If
   * it's able to parse the shape, {@link #offset} should be advanced beyond
   * it (e.g. to the ',' or ')' or EOF in general). The default implementation
   * checks the name against some predefined names and calls corresponding
   * parse methods to handle the rest. This method is an excellent extension
   * point for additional shape types.
   *
   * @param shapeType Non-Null string
   * @return The shape or null if not supported / unknown.
   * @throws ParseException
   */
  protected Shape parseShapeByType(String shapeType) throws ParseException {
    if (shapeType.equals("point")) {
      return parsePoint();
    }
    if (shapeType.equals("envelope")) {
      return parseEnvelope();
    }
    return null;
  }

  /**
   * Parses a Point Shape from the raw String.
   *
   * Point: 'POINT' '(' coordinate ')'
   *
   * @return Point Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Point correctly
   */
  private Shape parsePoint() throws ParseException {
    expect('(');
    Point coordinate = point();
    expect(')');
    return coordinate;
  }

  /**
   * Parses an Envelope Shape from the raw String.
   * Source: OGC "Catalogue Services Specification", the "CQL" (Common Query Language) sub-spec
   *
   * Envelope: 'ENVELOPE' '(' x1 ',' x2 ',' y2 ',' y1 ')'
   *
   * @return Envelope Shape parsed from the raw String
   * @throws ParseException Thrown if the raw String doesn't represent the Envelope correctly
   */
  protected Shape parseEnvelope() throws ParseException {
    expect('(');

    nextCharNoWS();
    double x1 = parseDouble();
    expect(',');
    nextCharNoWS();
    double x2 = parseDouble();
    expect(',');
    nextCharNoWS();
    double y2 = parseDouble();
    expect(',');
    nextCharNoWS();
    double y1 = parseDouble();

    expect(')');
    return ctx.makeRectangle(x1, x2, y1, y2);
  }

  /**
   * Reads a list of Points (AKA CoordinateSequence) from the current position.
   *
   * CoordinateSequence: '(' coordinate (',' coordinate )* ')'
   *
   * @return Points read from the current position. Non-null, non-empty.
   * @throws java.text.ParseException Thrown if reading the CoordinateSequence is unsuccessful
   */
  protected List<Point> pointList() throws ParseException {
    List<Point> sequence = new ArrayList<Point>();

    expect('(');
    sequence.add(point());

    while (nextCharNoWS() == ',') {
      offset++;
      sequence.add(point());
    }

    expect(')');
    return sequence;
  }

  /**
   * Reads a Point (AKA Coordinate) from the current position.
   *
   * Coordinate: number number
   *
   * @return The point read from the current position.
   * @throws java.text.ParseException Thrown if reading the Coordinate is unsuccessful
   */
  protected Point point() throws ParseException {
    // TODO: We need to validate the first character in the numbers
    nextCharNoWS();
    double x = parseDouble();

    nextCharNoWS();
    double y = parseDouble();

    return ctx.makePoint(x, y);
  }

  /**
   * Reads the word starting at the current character position. The word
   * terminates once {@link Character#isLetter(char)} returns false.
   *
   * @return Non-null non-empty String.
   * @throws ParseException if the word would otherwise be empty.
   */
  protected String nextWord() throws ParseException {
    int startOffset = offset;
    while (offset < rawString.length() && Character.isLetter(rawString.charAt
        (offset))) {
      offset++;
    }
    if (startOffset == offset)
      throw new ParseException("Word expected", startOffset);
    return rawString.substring(startOffset, offset);
  }

  /**
   * Reads in a double from the String.  Note, this method expects that the number
   * is delimited by some character.  Therefore if the String is exhausted before
   * a delimiter is encountered, a {@link ParseException} will be thrown.
   *
   * @return Double value
   * @throws ParseException Thrown if the String is exhausted before the number is delimited
   */
  protected double parseDouble() throws ParseException {
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
  protected void expect(char expected) throws ParseException {
    char c = nextCharNoWS();
    if (c != expected) {
      throw new ParseException("Expected [" + expected + "] found [" + c + "]", offset);
    }
    offset++;
  }

  /**
   * Returns the new character in the String which isn't whitespace. Does not
   * consume that character. This method is useful to position {@link #offset}
   * at the next non-whitespace.
   *
   * @return Next non-whitespace character
   * @throws ParseException Thrown if we reach the end of the String before reaching
   *         a non-whitespace character
   */
  protected char nextCharNoWS() throws ParseException {
    while (offset < rawString.length()) {
      if (!Character.isWhitespace(rawString.charAt(offset))) {
        return rawString.charAt(offset);
      }
      offset++;
    }

    throw new ParseException("EOF reached while expecting a non-whitespace character", offset);
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
    return  rawString.substring(startOffset, offset);
  }
}
