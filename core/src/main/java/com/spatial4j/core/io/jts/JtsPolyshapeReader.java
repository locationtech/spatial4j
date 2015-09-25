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

import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.io.PolyshapeReader;
import com.spatial4j.core.io.PolyshapeWriter;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JtsPolyshapeReader extends PolyshapeReader {

  protected final JtsSpatialContext ctx;

  public JtsPolyshapeReader(JtsSpatialContext ctx, SpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
  }
  

  @Override
  protected Shape makeCollection(List<? extends Shape> shapes) {
    Class<?> last = null;
    List<Geometry> geoms = new ArrayList<>(shapes.size());
    for(Shape s : shapes) {
      if(last!=null && last!=s.getClass()) {
        return super.makeCollection(shapes);
      }
      if(s instanceof JtsGeometry) {
        geoms.add(((JtsGeometry)s).getGeom());
      }
      else if(s instanceof JtsPoint) {
        geoms.add(((JtsPoint)s).getGeom());
      }
      else {
        return super.makeCollection(shapes);
      }
      last = s.getClass();
    }
    Geometry result = ctx.getGeometryFactory().buildGeometry(geoms);
    if(result.getClass().equals(GeometryCollection.class)) {
      return super.makeCollection(shapes);
    }
    // *not* calling makeShapeFromGeometry() since the underlying geometries here have
    //  already been converted to shapes via that method (or equivalent).
    return ctx.makeShape(result);
  }

  // --------------------------------------------------------------
  // Read GeoJSON
  // --------------------------------------------------------------

  protected CoordinateSequence coordseq(List<double[]> list) {
    CoordinateSequence seq =
        PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(list.size(), 2);

    for (int i = 0; i < list.size(); i++) {
      double[] point = list.get(i);
      seq.setOrdinate(i, 0, point[0]);
      seq.setOrdinate(i, 1, point[1]);
    }
    return seq;
  }
  
  @Override
  protected Shape readPolygon(XReader reader) throws IOException {
    GeometryFactory gf = ctx.getGeometryFactory();
    List<double[]> outer = reader.readPoints();

    LinearRing shell = gf.createLinearRing(coordseq(outer));
    LinearRing[] holes = null;
    if(!reader.isDone() && reader.peek()==PolyshapeWriter.KEY_ARG_START) {
      List<LinearRing> list = new ArrayList<LinearRing>();
      while(reader.isEvent() && reader.peek()==PolyshapeWriter.KEY_ARG_START) {
        reader.readKey(); // eat the event;
        list.add(gf.createLinearRing(coordseq(reader.readPoints())));
      }
      holes = list.toArray(new LinearRing[list.size()]);
    }
    return ctx.makeShapeFromGeometry(gf.createPolygon(shell, holes));
  }
}