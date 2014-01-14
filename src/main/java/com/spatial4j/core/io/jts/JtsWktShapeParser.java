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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.WktShapeParser;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extends {@link com.spatial4j.core.io.WktShapeParser} adding support for polygons, using JTS.
 */
public class JtsWktShapeParser extends WktShapeParser {

  protected final JtsSpatialContext ctx;

  protected final DatelineRule datelineRule;
  protected final ValidationRule validationRule;
  protected final boolean autoIndex;

  public JtsWktShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
    this.datelineRule = factory.datelineRule;
    this.validationRule = factory.validationRule;
    this.autoIndex = factory.autoIndex;
  }

  /** @see JtsWktShapeParser.ValidationRule */
  public ValidationRule getValidationRule() {
    return validationRule;
  }

  /**
   * JtsGeometry shapes are automatically validated when {@link #getValidationRule()} isn't
   * {@code none}.
   */
  public boolean isAutoValidate() {
    return validationRule != ValidationRule.none;
  }

  /**
   * If JtsGeometry shapes should be automatically prepared (i.e. optimized) when read via WKT.
   * @see com.spatial4j.core.shape.jts.JtsGeometry#index()
   */
  public boolean isAutoIndex() {
    return autoIndex;
  }


  /** @see DatelineRule */
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
    return makeShapeFromGeometry(geometryFactory.createLineString(coordinates));
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
    return makeShapeFromGeometry(geometry);
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
    double x = ctx.normX(state.nextDouble());
    ctx.verifyX(x);
    double y = ctx.normY(state.nextDouble());
    ctx.verifyY(y);
    state.skipNextDoubles();
    return new Coordinate(x, y);
  }

  @Override
  protected double normDist(double v) {
    return ctx.getGeometryFactory().getPrecisionModel().makePrecise(v);
  }

  /** Creates the JtsGeometry, potentially validating, repairing, and preparing. */
  protected JtsGeometry makeShapeFromGeometry(Geometry geometry) {
    final boolean dateline180Check = getDatelineRule() != DatelineRule.none;
    JtsGeometry jtsGeom;
    try {
      jtsGeom = ctx.makeShape(geometry, dateline180Check, ctx.isAllowMultiOverlap());
      if (isAutoValidate())
        jtsGeom.validate();
    } catch (RuntimeException e) {
      //repair:
      if (validationRule == ValidationRule.repairConvexHull) {
        jtsGeom = ctx.makeShape(geometry.convexHull(), dateline180Check, ctx.isAllowMultiOverlap());
      } else if (validationRule == ValidationRule.repairBuffer0) {
        jtsGeom = ctx.makeShape(geometry.buffer(0), dateline180Check, ctx.isAllowMultiOverlap());
      } else {
        //TODO there are other smarter things we could do like repairing inner holes and subtracting
        //  from outer repaired shell; but we needn't try too hard.
        throw e;
      }
    }
    if (isAutoIndex())
      jtsGeom.index();
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

    /** For rectangular polygons, the point order is interpreted as being counter-clockwise (CCW).
     * However, non-rectangular polygons or other shapes aren't processed this way; they use the
     * {@link #width180} rule instead. The CCW rule is specified by OGC Simple Features
     * Specification v. 1.2.0 section 6.1.11.1.
     */
    ccwRect
  }

  /** Indicates how JTS geometries (notably polygons but applies to other geometries too) are
   * validated (if at all) and repaired (if at all).
   */
  public enum ValidationRule {
    /** Geometries will not be validated (because it's kinda expensive to calculate). You may or may
     * not ultimately get an error at some point; results are undefined. However, note that
     * coordinates will still be validated for falling within the world boundaries.
     * @see com.vividsolutions.jts.geom.Geometry#isValid(). */
    none,

    /** Geometries will be explicitly validated on creation, possibly resulting in an exception:
     * {@link com.spatial4j.core.exception.InvalidShapeException}. */
    error,

    /** Invalid Geometries are repaired by taking the convex hull. The result will very likely be a
     * larger shape that matches false-positives, but no false-negatives.
     * See {@link com.vividsolutions.jts.geom.Geometry#convexHull()}. */
    repairConvexHull,

    /** Invalid polygons are repaired using the {@code buffer(0)} technique. From the <a
     * href="http://tsusiatsoftware.net/jts/jts-faq/jts-faq.html">JTS FAQ</a>:
     * <p>The buffer operation is fairly insensitive to topological invalidity, and the act of
     * computing the buffer can often resolve minor issues such as self-intersecting rings. However,
     * in some situations the computed result may not be what is desired (i.e. the buffer operation
     * may be "confused" by certain topologies, and fail to produce a result which is close to the
     * original. An example where this can happen is a "bow-tie: or "figure-8" polygon, with one
     * very small lobe and one large one. Depending on the orientations of the lobes, the buffer(0)
     * operation may keep the small lobe and discard the "valid" large lobe).
     * </p> */
    repairBuffer0
  }
}
