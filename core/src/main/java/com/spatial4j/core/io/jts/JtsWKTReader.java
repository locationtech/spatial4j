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

import com.spatial4j.core.context.jts.DatelineRule;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.WKTReader;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
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

  public JtsWKTReader(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
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
    return ctx.makeShapeFromGeometry(geometryFactory.createLineString(coordinates));
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
        return ctx.makeRectFromRectangularPoly(geometry);
      }
    }
    return ctx.makeShapeFromGeometry(geometry);
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

}
