/*******************************************************************************
 * Copyright (c) 2015 ElasticSearch and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

// A derivative of commit 14bc4dee08355048d6a94e33834b919a3999a06e
//  at https://github.com/chrismale/elasticsearch

package com.spatial4j.core.io.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.DatelineRule;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.WKTReader;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extends {@link com.spatial4j.core.io.WKTReader} adding support for polygons, using JTS.
 */
public class JtsWKTReader extends WKTReader {

  protected final JtsSpatialContext ctx;

  protected final ValidationRule validationRule;
  protected final boolean autoIndex;

  public JtsWKTReader(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
    this.validationRule = factory.validationRule;
    this.autoIndex = factory.autoIndex;
  }

  /** @see JtsWKTReader.ValidationRule */
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
   * 
   * @see com.spatial4j.core.shape.jts.JtsGeometry#index()
   */
  public boolean isAutoIndex() {
    return autoIndex;
  }


  /** @see DatelineRule */
  public DatelineRule getDatelineRule() {
    return ctx.getDatelineRule();
  }

  @Override
  protected Shape parseShapeByType(WKTReader.State state, String shapeType) throws ParseException {
    if (shapeType.equalsIgnoreCase("POLYGON")) {
      return parsePolygonShape(state);
    } else if (shapeType.equalsIgnoreCase("MULTIPOLYGON")) {
      return parseMulitPolygonShape(state);
    }
    return super.parseShapeByType(state, shapeType);
  }

  /**
   * Bypasses {@link JtsSpatialContext#makeLineString(java.util.List)} so that we can more
   * efficiently get the LineString without creating a {@code List<Point>}.
   */
  @Override
  protected Shape parseLineStringShape(WKTReader.State state) throws ParseException {
    if (!ctx.useJtsLineString())
      return super.parseLineStringShape(state);

    if (state.nextIfEmptyAndSkipZM())
      return ctx.makeLineString(Collections.<Point>emptyList());

    GeometryFactory geometryFactory = ctx.getGeometryFactory();

    Coordinate[] coordinates = coordinateSequence(state);
    return makeShapeFromGeometry(geometryFactory.createLineString(coordinates));
  }

  /**
   * Parses a POLYGON shape from the raw string. It might return a
   * {@link com.spatial4j.core.shape.Rectangle} if the polygon is one.
   * 
   * <pre>
   * coordinateSequenceList
   * </pre>
   */
  protected Shape parsePolygonShape(WKTReader.State state) throws ParseException {
    Geometry geometry;
    if (state.nextIfEmptyAndSkipZM()) {
      GeometryFactory geometryFactory = ctx.getGeometryFactory();
      geometry =
          geometryFactory
              .createPolygon(geometryFactory.createLinearRing(new Coordinate[] {}), null);
    } else {
      geometry = polygon(state);
      if (geometry.isRectangle()) {
        // TODO although, might want to never convert if there's a semantic difference (e.g.
        // geodetically)
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
        crossesDateline = !CGAlgorithms.isCCW(geometry.getCoordinates());
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
  protected Polygon polygon(WKTReader.State state) throws ParseException {
    GeometryFactory geometryFactory = ctx.getGeometryFactory();

    List<Coordinate[]> coordinateSequenceList = coordinateSequenceList(state);

    LinearRing shell = geometryFactory.createLinearRing(coordinateSequenceList.get(0));

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
   * 
   * <pre>
   *   '(' polygon (',' polygon )* ')'
   * </pre>
   */
  protected Shape parseMulitPolygonShape(WKTReader.State state) throws ParseException {
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
   * 
   * <pre>
   *   '(' coordinateSequence (',' coordinateSequence )* ')'
   * </pre>
   */
  protected List<Coordinate[]> coordinateSequenceList(WKTReader.State state) throws ParseException {
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
   * 
   * <pre>
   *   '(' coordinate (',' coordinate )* ')'
   * </pre>
   */
  protected Coordinate[] coordinateSequence(WKTReader.State state) throws ParseException {
    List<Coordinate> sequence = new ArrayList<Coordinate>();
    state.nextExpect('(');
    do {
      sequence.add(coordinate(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return sequence.toArray(new Coordinate[sequence.size()]);
  }

  /**
   * Reads a {@link com.vividsolutions.jts.geom.Coordinate} from the current position. It's akin to
   * {@link #point(com.spatial4j.core.io.WKTReader.State)} but for a JTS Coordinate. Only the first
   * 2 numbers are parsed; any remaining are ignored.
   */
  protected Coordinate coordinate(WKTReader.State state) throws ParseException {
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

  /**
   * Usually creates a JtsGeometry, potentially validating, repairing, and preparing.
   *
   * If given a direct instance of {@link GeometryCollection} then it's contents will be
   * recursively converted and then the resulting list will be passed to
   * {@link SpatialContext#makeCollection(List)} and returned.
   *
   * If given a {@link com.vividsolutions.jts.geom.Point} then {@link SpatialContext#makePoint(double, double)}
   * is called, which will return a {@link JtsPoint} if {@link JtsSpatialContext#useJtsPoint()}; otherwise
   * a standard Spatial4j Point is returned.
   *
   * If given a {@link LineString} and if {@link JtsSpatialContext#useJtsLineString()} is true then
   * then the geometry's parts are exposed to call {@link SpatialContext#makeLineString(List)}.
   */
  public Shape makeShapeFromGeometry(Geometry geometry) {
    // Direct instances of GeometryCollection can't be wrapped in JtsGeometry but can be expanded into
    //  a ShapeCollection.
    if (geometry.getClass() == GeometryCollection.class) {
      List<Shape> shapes = new ArrayList<>(geometry.getNumGeometries());
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Geometry geomN = geometry.getGeometryN(i);
        shapes.add(makeShapeFromGeometry(geomN));//recursion
      }
      return ctx.makeCollection(shapes);
    }
    if (geometry instanceof com.vividsolutions.jts.geom.Point) {
      com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point) geometry;
      return ctx.makePoint(pt.getX(), pt.getY());
    }
    if (!ctx.useJtsLineString() && geometry instanceof LineString) {
      LineString lineString = (LineString) geometry;
      List<Point> points = new ArrayList<>(lineString.getNumPoints());
      for (int i = 0; i < lineString.getNumPoints(); i++) {
        Coordinate coord = lineString.getCoordinateN(i);
        points.add(ctx.makePoint(coord.x, coord.y));
      }
      return ctx.makeLineString(points);
    }

    JtsGeometry jtsGeom;
    try {
      jtsGeom = ctx.makeShape(geometry);
      if (isAutoValidate())
        jtsGeom.validate();
    } catch (RuntimeException e) {
      // repair:
      if (validationRule == ValidationRule.repairConvexHull) {
        jtsGeom = ctx.makeShape(geometry.convexHull());
      } else if (validationRule == ValidationRule.repairBuffer0) {
        jtsGeom = ctx.makeShape(geometry.buffer(0));
      } else {
        // TODO there are other smarter things we could do like repairing inner holes and
        // subtracting
        // from outer repaired shell; but we needn't try too hard.
        throw e;
      }
    }
    if (isAutoIndex())
      jtsGeom.index();
    return jtsGeom;
  }

  /**
   * Indicates how JTS geometries (notably polygons but applies to other geometries too) are
   * validated (if at all) and repaired (if at all).
   */
  public enum ValidationRule {
    /**
     * Geometries will not be validated (because it's kinda expensive to calculate). You may or may
     * not ultimately get an error at some point; results are undefined. However, note that
     * coordinates will still be validated for falling within the world boundaries.
     * 
     * @see com.vividsolutions.jts.geom.Geometry#isValid().
     */
    none,

    /**
     * Geometries will be explicitly validated on creation, possibly resulting in an exception:
     * {@link com.spatial4j.core.exception.InvalidShapeException}.
     */
    error,

    /**
     * Invalid Geometries are repaired by taking the convex hull. The result will very likely be a
     * larger shape that matches false-positives, but no false-negatives. See
     * {@link com.vividsolutions.jts.geom.Geometry#convexHull()}.
     */
    repairConvexHull,

    /**
     * Invalid polygons are repaired using the {@code buffer(0)} technique. From the <a
     * href="http://tsusiatsoftware.net/jts/jts-faq/jts-faq.html">JTS FAQ</a>:
     * <p>
     * The buffer operation is fairly insensitive to topological invalidity, and the act of
     * computing the buffer can often resolve minor issues such as self-intersecting rings. However,
     * in some situations the computed result may not be what is desired (i.e. the buffer operation
     * may be "confused" by certain topologies, and fail to produce a result which is close to the
     * original. An example where this can happen is a "bow-tie: or "figure-8" polygon, with one
     * very small lobe and one large one. Depending on the orientations of the lobes, the buffer(0)
     * operation may keep the small lobe and discard the "valid" large lobe).
     * </p>
     */
    repairBuffer0
  }
}
