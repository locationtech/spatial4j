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

import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.io.PolyshapeWriter;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JtsPolyshapeWriter extends PolyshapeWriter {

  protected final JtsSpatialContext ctx;

  public JtsPolyshapeWriter(JtsSpatialContext ctx, SpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
  }

  // --------------------------------------------------------------
  // Write JTS To GeoJSON
  // --------------------------------------------------------------

  protected void write(Encoder output, CoordinateSequence coordseq) throws IOException {
    int dim = coordseq.getDimension();
//    if(dim>2) {
//      throw new IllegalArgumentException("only supports 2d geometry now ("+dim+")");
//    }
    for (int i = 0; i < coordseq.size(); i++) {
      output.write(coordseq.getOrdinate(i, 0),
                   coordseq.getOrdinate(i, 1));
    }
  }

  protected void write(Encoder output, Coordinate[] coord) throws IOException {
    for (int i = 0; i < coord.length; i++) {
      output.write(coord[i].x, coord[i].y);
    }
  }

  protected void write(Encoder output, Polygon p) throws IOException {
    output.write(PolyshapeWriter.KEY_POLYGON);
    write(output, p.getExteriorRing().getCoordinateSequence());
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      output.startRing();
      write(output, p.getInteriorRingN(i).getCoordinateSequence());
    }
  }

  public void write(Encoder output, Geometry geom) throws IOException {
    if (geom instanceof Point) {
      Point v = (Point) geom;
      output.write(PolyshapeWriter.KEY_POINT);
      write(output, v.getCoordinateSequence());
      return;
    } else if (geom instanceof Polygon) {
      write(output, (Polygon) geom);
      return;
    } else if (geom instanceof LineString) {
      LineString v = (LineString) geom;
      output.write(PolyshapeWriter.KEY_LINE);
      write(output, v.getCoordinateSequence());
      return;
    } else if (geom instanceof MultiPoint) {
      MultiPoint v = (MultiPoint) geom;
      output.write(PolyshapeWriter.KEY_MULTIPOINT);
      write(output, v.getCoordinates());
      return;
    } else if (geom instanceof GeometryCollection) {
      GeometryCollection v = (GeometryCollection) geom;
      for (int i = 0; i < v.getNumGeometries(); i++) {
        if (i > 0) {
          output.seperator();
        }
        write(output, v.getGeometryN(i));
      }
    } else {
      throw new UnsupportedOperationException("unknown: " + geom);
    }
  }

  @Override
  public void write(Encoder enc, Shape shape) throws IOException {
    if (shape == null) {
      throw new NullPointerException("Shape can not be null");
    }
    if (shape instanceof JtsGeometry) {
      write(enc, ((JtsGeometry) shape).getGeom());
      return;
    }
    super.write(enc, shape);
  }
}