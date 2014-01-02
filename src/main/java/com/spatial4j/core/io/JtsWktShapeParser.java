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
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extends {@link WktShapeParser} adding support for polygons, using JTS.
 */
public class JtsWktShapeParser extends WktShapeParser {

  protected final JtsSpatialContext ctx;

  protected final JtsWktShapeParser.DatelineRule datelineRule;
  protected final boolean autoValidate;
  protected final boolean autoPrepare;

  public JtsWktShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
    this.datelineRule = factory.datelineRule;
    this.autoValidate = factory.autoValidate;
    this.autoPrepare = factory.autoPrepare;
  }

  /**
   * If JtsGeometry shapes should be automatically validated when read via WKT.
   * @see com.spatial4j.core.shape.jts.JtsGeometry#validate()
   */
  public boolean isAutoValidate() {
    return autoValidate;
  }

  /**
   * If JtsGeometry shapes should be automatically prepared (i.e. optimized) when read via WKT.
   * @see com.spatial4j.core.shape.jts.JtsGeometry#prepare()
   */
  public boolean isAutoPrepare() {
    return autoPrepare;
  }


  /** See {@link DatelineRule}. */
  public DatelineRule getDatelineRule() {
    return datelineRule;
  }

  @Override
  protected Shape parseShapeByType(WktShapeParser.State state, String shapeType) throws ParseException {
    if (shapeType.equalsIgnoreCase("POLYGON")) {
      return parsePolygonShape(state);
    } else if (shapeType.equalsIgnoreCase("MULTIPOLYGON")) {
      return parseMulitPolygonShape(state);
    }
    return super.parseShapeByType(state, shapeType);
  }

  /** Bypasses {@link JtsSpatialContext#makeLineString(java.util.List)} so that we can more
   * efficiently get the LineString without creating a {@code List<Point>}.
   */
  @Override
  protected Shape parseLineStringShape(WktShapeParser.State state) throws ParseException {
    if (!ctx.useJtsLineString())
      return super.parseLineStringShape(state);

    if (state.nextIfEmptyAndSkipZM())
      return ctx.makeLineString(Collections.<Point>emptyList());

    GeometryFactory geometryFactory = ctx.getGeometryFactory();

    Coordinate[] coordinates = coordinateSequence(state);
    return makeShapeAndMaybeValidate(geometryFactory.createLineString(coordinates));
  }

  /**
   * Parses a POLYGON shape from the raw string. It might return a {@link com.spatial4j.core.shape.Rectangle}
   * if the polygon is one.
   * <pre>
   *   coordinateSequenceList
   * </pre>
   */
  protected Shape parsePolygonShape(WktShapeParser.State state) throws ParseException {
    Geometry geometry;
    if (state.nextIfEmptyAndSkipZM()) {
      GeometryFactory geometryFactory = ctx.getGeometryFactory();
      geometry = geometryFactory.createPolygon(geometryFactory.createLinearRing(
          new Coordinate[]{}), null);
    } else {
      geometry = polygon(state);
      if (geometry.isRectangle()) {
        //TODO although, might want to never convert if there's a semantic difference (e.g. geodetically)
        return makeRectFromPoly(geometry);
      }
    }
    return makeShapeAndMaybeValidate(geometry);
  }

  protected Rectangle makeRectFromPoly(Geometry geometry) {
    assert geometry.isRectangle();
    Envelope env = geometry.getEnvelopeInternal();
    boolean crossesDateline = false;
    if (ctx.isGeo() && getDatelineRule() != DatelineRule.none) {
      if (getDatelineRule() == DatelineRule.ccwRect) {
        // If JTS says it is clockwise, then it's actually a dateline crossing rectangle.
        crossesDateline = ! CGAlgorithms.isCCW(geometry.getCoordinates());
      } else {
        crossesDateline = env.getWidth() > 180;
      }
    }
    if (crossesDateline)
      return ctx.makeRectangle(env.getMaxX(), env.getMinX(), env.getMinY(), env.getMaxY());
    else
      return ctx.makeRectangle(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
  }

  /**
   * Reads a polygon, returning a JTS polygon.
   */
  protected Polygon polygon(WktShapeParser.State state) throws ParseException {
    GeometryFactory geometryFactory = ctx.getGeometryFactory();

    List<Coordinate[]> coordinateSequenceList = coordinateSequenceList(state);

    LinearRing shell = geometryFactory.createLinearRing
        (coordinateSequenceList.get(0));

    LinearRing[] holes = null;
    if (coordinateSequenceList.size() > 1) {
      holes = new LinearRing[coordinateSequenceList.size() - 1];
      for (int i = 1; i < coordinateSequenceList.size(); i++) {
        holes[i - 1] = geometryFactory.createLinearRing(coordinateSequenceList.get(i));
      }
    }
    return geometryFactory.createPolygon(shell, holes);
  }

  /**
   * Parses a MULTIPOLYGON shape from the raw string.
   * <pre>
   *   '(' polygon (',' polygon )* ')'
   * </pre>
   */
  protected Shape parseMulitPolygonShape(WktShapeParser.State state) throws ParseException {
    if (state.nextIfEmptyAndSkipZM())
      return ctx.makeCollection(Collections.EMPTY_LIST);

    List<Shape> polygons = new ArrayList<Shape>();
    state.nextExpect('(');
    do {
      polygons.add(parsePolygonShape(state));
    } while (state.nextIf(','));
    state.nextExpect(')');

    return ctx.makeCollection(polygons);
  }


  /**
   * Reads a list of JTS Coordinate sequences from the current position.
   * <pre>
   *   '(' coordinateSequence (',' coordinateSequence )* ')'
   * </pre>
   */
  protected List<Coordinate[]> coordinateSequenceList(WktShapeParser.State state) throws ParseException {
    List<Coordinate[]> sequenceList = new ArrayList<Coordinate[]>();
    state.nextExpect('(');
    do {
      sequenceList.add(coordinateSequence(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return sequenceList;
  }

  /**
   * Reads a JTS Coordinate sequence from the current position.
   * <pre>
   *   '(' coordinate (',' coordinate )* ')'
   * </pre>
   */
  protected Coordinate[] coordinateSequence(WktShapeParser.State state) throws ParseException {
    List<Coordinate> sequence = new ArrayList<Coordinate>();
    state.nextExpect('(');
    do {
      sequence.add(coordinate(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return sequence.toArray(new Coordinate[sequence.size()]);
  }

  /**
   * Reads a {@link com.vividsolutions.jts.geom.Coordinate} from the current position.
   * It's akin to {@link #point(com.spatial4j.core.io.WktShapeParser.State)} but for
   * a JTS Coordinate.  Only the first 2 numbers are parsed; any remaining are ignored.
   */
  protected Coordinate coordinate(WktShapeParser.State state) throws ParseException {
    double x = state.nextDouble();
    double y = state.nextDouble();
    state.skipNextDoubles();
    return new Coordinate(ctx.normX(x), ctx.normY(y));
  }

  protected JtsGeometry makeShapeAndMaybeValidate(Geometry geometry) {
    boolean dateline180Check = getDatelineRule() != DatelineRule.none;
    JtsGeometry jtsGeom = ctx.makeShape(geometry, dateline180Check, ctx.isAllowMultiOverlap());
    if (isAutoValidate()) jtsGeom.validate();
    if (isAutoPrepare()) jtsGeom.prepare();
    return jtsGeom;
  }

  /**
   * Indicates the algorithm used to process JTS Polygons and JTS LineStrings for detecting dateline
   * crossings. It only applies when geo=true.
   */
  public enum DatelineRule {
    /** No polygon will cross the dateline. */
    none,

    /** Adjacent points with an x (longitude) difference that spans more than half
     * way around the globe will be interpreted as going the other (shorter) way, and thus cross the
     * dateline.
     */
    width180,//TODO is there a better name that doesn't have '180' in it?

    /** For rectangular polygons, the point order is interpreted as being Counter-Clockwise (which
     * is consistent with OGC Simple Features Specification v. 1.2.0 section 6.1.11.1).
     * However, non-rectangular polygons or other shapes aren't processed this way; they use the
     * {@link #width180} rule instead. */
    ccwRect
  }
}
