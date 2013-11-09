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
 *   <li>MULTIPOINT</li>
 *   <li>ENVELOPE</li>
 *   <li>GEOMETRYCOLLECTION</li>
 * </ul>
 * <p />
 * To support more shapes, extend this class and override {@link #parseShapeByType(WktShapeParser.State, String)}.
 * <p />
 * Note, instances are threadsafe.
 */
public class WktShapeParser {

  //TODO
  // * EMPTY shapes  (new EmptyShape with name?)
  // * SRID:    "SRID=4326;pointPOINT(1,2)
  // * ZM, M, Z, other-dimensions?
  //  ex: POINT Z (3,2)      POINT Z EMPTY      POINT EMPTY      POINT M (3,2)     POINT ZM (3,2)

  //TODO should reference proposed ShapeFactory instead of ctx, which is a point of indirection that
  // might optionally do data validation & normalization
  protected final SpatialContext ctx;

  public WktShapeParser(SpatialContext ctx) {
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
    throw new ParseException("Unknown Shape definition [" + shortenedString + "]", 0);
  }

  /**
   * Parses the wktString, returning the defined Shape. If it can't because the
   * shape name is unknown or an empty or blank string was passed, then it returns null.
   * If the WKT starts with a supported shape but contains an inner unsupported shape then
   * it will result in a {@link ParseException}.
   *
   * @param wktString non-null, can be empty or have surrounding whitespace
   * @return Shape, null if unknown / unsupported shape.
   * @throws ParseException Thrown if there is an error in the Shape definition
   */
  public Shape parseIfSupported(String wktString) throws ParseException {
    State state = newState(wktString);
    state.nextIfWhitespace();//leading
    if (state.offset >= state.rawString.length())
      return null;
    if (!Character.isLetter(wktString.charAt(state.offset)))//optimization short-circuit
      return null;

    String shapeType = state.nextWord();
    Shape result = parseShapeByType(state, shapeType);
    if (result != null) {
      if (state.offset != wktString.length())
        throw new ParseException("end of shape expected", state.offset);
    }
    return result;
  }

  /** Creates a new State with the given String. This is an extension point for subclassing. */
  protected State newState(String wktString) {
    return new State(wktString);
  }

  /**
   * Parses the remainder of a shape definition following the shape's name
   * given as {@code shapeType} already consumed via
   * {@link #nextWord()}. If
   * it's able to parse the shape, {@link WktShapeParser.State#offset}
   * should be advanced beyond
   * it (e.g. to the ',' or ')' or EOF in general). The default implementation
   * checks the name against some predefined names and calls corresponding
   * parse methods to handle the rest. Overriding this method is an
   * excellent extension point for additional shape types. Or use this class by delegation to this
   * method.
   *
   * @param state
   * @param shapeType Non-Null string; could have mixed case. The first character is a letter.
   * @return The shape or null if not supported / unknown.
   */
  public Shape parseShapeByType(State state, String shapeType) throws ParseException {
    assert Character.isLetter(shapeType.charAt(0)) : "Shape must start with letter: "+shapeType;

    if (shapeType.equalsIgnoreCase("POINT")) {
      return parsePointShape(state);
    } else if (shapeType.equalsIgnoreCase("MULTIPOINT")) {
      return parseMultiPointShape(state);
    } else if (shapeType.equalsIgnoreCase("ENVELOPE")) {
      return parseEnvelopeShape(state);
    } else if (shapeType.equalsIgnoreCase("GEOMETRYCOLLECTION")) {
      return parseGeometryCollectionShape(state);
    } else if (shapeType.equalsIgnoreCase("LINESTRING")) {
      return parseLineStringShape(state);
    } else if (shapeType.equalsIgnoreCase("MULTILINESTRING")) {
      return parseMultiLineStringShape(state);
    }
    return null;
  }

  /**
   * Parses a Point Shape from the raw String.
   * <pre>
   * 'POINT' '(' coordinate ')'
   * </pre>
   *
   * @see #point(WktShapeParser.State)
   */
  protected Shape parsePointShape(State state) throws ParseException {
    state.nextExpect('(');
    Point coordinate = point(state);
    state.nextExpect(')');
    return coordinate;
  }

  /**
   * Parses a MULTIPOINT shape -- a collection of points.
   * <pre>
   * 'MULTIPOINT' '(' coordinate (',' coordinate )* ')'
   * </pre>
   * Furthermore, coordinate can optionally be wrapped in parenthesis.
   *
   * @see #point(WktShapeParser.State)
   */
  protected Shape parseMultiPointShape(State state) throws ParseException {
    List<Point> shapes = new ArrayList<Point>();
    state.nextExpect('(');
    do {
      boolean openParen = state.nextIf('(');
      Point coordinate = point(state);
      if (openParen)
        state.nextExpect(')');
      shapes.add(coordinate);
    } while (state.nextIf(','));
    state.nextExpect(')');
    return ctx.makeCollection(shapes);
  }

  /**
   * Parses an Envelope (Rectangle) Shape from the raw String.
   * <p />
   * Source: OGC "Catalogue Services Specification", the "CQL" (Common Query Language) sub-spec.
   * <em>Note the inconsistent order of the min & max values between x & y!</em>
   * <pre>
   * 'ENVELOPE' '(' x1 ',' x2 ',' y2 ',' y1 ')'
   * </pre>
   */
  protected Shape parseEnvelopeShape(State state) throws ParseException {
    state.nextExpect('(');
    double x1 = state.nextDouble();
    state.nextExpect(',');
    double x2 = state.nextDouble();
    state.nextExpect(',');
    double y2 = state.nextDouble();
    state.nextExpect(',');
    double y1 = state.nextDouble();
    state.nextExpect(')');
    return ctx.makeRectangle(x1, x2, y1, y2);
  }

  /**
   * Parses a LINESTRING shape -- an ordered sequence of points.
   * <pre>
   *   'LINESTRING' coordinateSequence
   * </pre>
   *
   * @see #pointList(WktShapeParser.State)
   */
  protected Shape parseLineStringShape(State state) throws ParseException {
    List<Point> points = pointList(state);
    return ctx.makeLineString(points);
  }

  /**
   * Reads a ShapeCollection (AKA GeometryCollection) from the raw string.
   * <pre>
   * 'MULTILINESTRING' '(' coordinateSequence (',' coordinateSequence )* ')'
   * </pre>
   *
   * @see #pointList(WktShapeParser.State)
   */
  protected Shape parseMultiLineStringShape(State state) throws ParseException {
    List<Shape> shapes = new ArrayList<Shape>();
    state.nextExpect('(');
    do {
      shapes.add(parseLineStringShape(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return ctx.makeCollection(shapes);
  }

  /**
   * Reads a ShapeCollection (AKA GeometryCollection) from the raw string.
   * <pre>
   * 'GEOMETRYCOLLECTION' '(' shape (',' shape )* ')'
   * </pre>
   */
  protected Shape parseGeometryCollectionShape(State state) throws ParseException {
    List<Shape> shapes = new ArrayList<Shape>();
    state.nextExpect('(');
    do {
      shapes.add(shape(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return ctx.makeCollection(shapes);
  }

  /** Reads a shape from the current position, starting with the name of the shape. */
  protected Shape shape(State state) throws ParseException {
    String type = state.nextWord();
    Shape shape = parseShapeByType(state, type);
    if (shape == null)
      throw new ParseException("Shape of type "+type+" is unknown", state.offset);
    return shape;
  }

  /**
   * Reads a list of Points (AKA CoordinateSequence) from the current position.
   * <pre>
   * '(' coordinate (',' coordinate )* ')'
   * </pre>
   *
   * @see #point(WktShapeParser.State)
   */
  protected List<Point> pointList(State state) throws ParseException {
    List<Point> sequence = new ArrayList<Point>();
    state.nextExpect('(');
    do {
      sequence.add(point(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return sequence;
  }

  /**
   * Reads a raw Point (AKA Coordinate) from the current position.
   * <pre>
   * number number
   * </pre>
   */
  protected Point point(State state) throws ParseException {
    double x = state.nextDouble();
    double y = state.nextDouble();
    return ctx.makePoint(x, y);
  }

  /** The parse state. */
  public class State {
    /**
     * Set in {@link #parseIfSupported(String)}.
     */
    public String rawString;
    /**
     * Offset of the next char in {@link #rawString} to be read.
     */
    public int offset;

    public State(String rawString) {
      this.rawString = rawString;
    }

    public SpatialContext getCtx() { return ctx; }

    public WktShapeParser getParser() { return WktShapeParser.this; }

    /**
     * Reads the word starting at the current character position. The word
     * terminates once {@link Character#isJavaIdentifierPart(char)} returns false (or EOF).
     * {@link #offset} is advanced past whitespace.
     *
     * @return Non-null non-empty String.
     */
    public String nextWord() throws ParseException {
      int startOffset = offset;
      while (offset < rawString.length() &&
          Character.isJavaIdentifierPart(rawString.charAt(offset))) {
        offset++;
      }
      if (startOffset == offset)
        throw new ParseException("Word expected", startOffset);
      String result = rawString.substring(startOffset, offset);
      nextIfWhitespace();
      return result;
    }

    /**
     * Reads in a double from the String. Parses digits with an optional decimal, sign, or exponent.
     * NaN and Infinity are not supported.
     * {@link #offset} is advanced past whitespace.
     *
     * @return Double value
     */
    public double nextDouble() throws ParseException {
      int startOffset = offset;
      for (; offset < rawString.length(); offset++) {
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
      nextIfWhitespace();
      return result;
    }

    /**
     * Verifies that the current character is of the expected value.
     * If the character is the expected value, then it is consumed and
     * {@link #offset} is advanced past whitespace.
     *
     * @param expected The expected char.
     */
    public void nextExpect(char expected) throws ParseException {
      if (offset >= rawString.length())
        throw new ParseException("Expected [" + expected + "] found EOF", offset);
      char c = rawString.charAt(offset);
      if (c != expected)
        throw new ParseException("Expected [" + expected + "] found [" + c + "]", offset);
      offset++;
      nextIfWhitespace();
    }

    /**
     * If the current character is {@code expected}, then offset is advanced after it and any
     * subsequent whitespace. Otherwise, false is returned.
     *
     * @param expected The expected char
     * @return true if consumed
     */
    public boolean nextIf(char expected) {
      if (offset < rawString.length() && rawString.charAt(offset) == expected) {
        offset++;
        nextIfWhitespace();
        return true;
      }
      return false;
    }

    /**
     * Moves offset to next non-whitespace character. Doesn't move if the offset is already at
     * non-whitespace. <em>There is very little reason for subclasses to call this because
     * most other parsing methods call it.</em>
     */
    public void nextIfWhitespace() {
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
    public String nextSubShapeString() throws ParseException {
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
  }//class State
}