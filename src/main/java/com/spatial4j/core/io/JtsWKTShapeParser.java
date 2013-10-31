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
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the default parser to add support for polygons.
 */
public class JtsWKTShapeParser extends WKTShapeParser {

  /** See {@link JtsGeometry} */
  protected boolean dateline180Check = true;

  public JtsWKTShapeParser(JtsSpatialContext ctx) {
    super(ctx);
  }

  @Override
  public JtsSpatialContext getCtx() {
    return (JtsSpatialContext) super.getCtx();
  }

  private GeometryFactory getGeometryFactory() {
    return getCtx().getGeometryFactory();
  }

  @Override
  protected Shape parseShapeByType(State state, String shapeType) throws ParseException {
    if (shapeType.equalsIgnoreCase("POLYGON")) {
      return parsePolygonShape(state);
    } else if (shapeType.equalsIgnoreCase("MULTIPOLYGON")) {
      return parseMulitPolygonShape(state);
    }
    return super.parseShapeByType(state, shapeType);
  }

  protected JtsGeometry parsePolygonShape(State state) throws ParseException {
    return new JtsGeometry(polygon(state), getCtx(), dateline180Check);
  }

  /**
   * Parses a Polygon Shape from the raw String
   *
   * Polygon: 'POLYGON' coordinateSequenceList
   * @param state
   */
  protected Polygon polygon(State state) throws ParseException {
    List<Coordinate[]> coordinateSequenceList = coordinateSequenceList(state);

    LinearRing shell = getGeometryFactory().createLinearRing
        (coordinateSequenceList.get(0));

    LinearRing[] holes = null;
    if (coordinateSequenceList.size() > 1) {
      holes = new LinearRing[coordinateSequenceList.size() - 1];
      for (int i = 1; i < coordinateSequenceList.size(); i++) {
        holes[i - 1] = getGeometryFactory().createLinearRing(coordinateSequenceList.get(i));
      }
    }
    return getGeometryFactory().createPolygon(shell, holes);
  }


  /**
   * Parses a MultiPolygon Shape from the raw String
   *
   * MultiPolygon: 'MULTIPOLYGON' '(' coordinateSequenceList (',' coordinateSequenceList )* ')'
   * @param state
   */
  protected Shape parseMulitPolygonShape(State state) throws ParseException {
    List<Polygon> polygons = new ArrayList<Polygon>();
    state.nextExpect('(');
    do {
      polygons.add(polygon(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return new JtsGeometry(
        getGeometryFactory().createMultiPolygon(polygons.toArray(new
            Polygon[polygons.size()])),
        getCtx(), dateline180Check);
  }

  /**
   * Reads a CoordinateSequenceList from the current position
   *
   * CoordinateSequenceList: '(' coordinateSequence (',' coordinateSequence )* ')'
   * @param state
   */
  protected List<Coordinate[]> coordinateSequenceList(State state) throws ParseException {
    List<Coordinate[]> sequenceList = new ArrayList<Coordinate[]>();
    state.nextExpect('(');
    do {
      sequenceList.add(coordinateSequence(state));
    } while (state.nextIf(','));
    state.nextExpect(')');
    return sequenceList;
  }

  /**
   * Reads a CoordinateSequence from the current position
   *
   * CoordinateSequence: '(' coordinate (',' coordinate )* ')'
   * @param state
   */
  protected Coordinate[] coordinateSequence(State state) throws ParseException {
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
   *
   * Coordinate: number number
   * @param state
   */
  protected Coordinate coordinate(State state) throws ParseException {
    double x = state.nextDouble();
    double y = state.nextDouble();
    return new Coordinate(x, y);
  }
}
