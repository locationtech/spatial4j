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
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import java.text.ParseException;

/**
 * This is an extension of {@link JtsWktShapeParser} that processes the entire
 * string with JTS's {@link com.vividsolutions.jts.io.WKTReader}.  Some differences:
 * <ul>
 *   <li>No support for ENVELOPE and BUFFER</li>
 *   <li>MULTI* shapes use JTS's {@link com.vividsolutions.jts.geom.GeometryCollection} subclasses,
 *   not {@link com.spatial4j.core.shape.ShapeCollection}</li>
 *   <li>'Z' coordinates are saved into the geometry</li>
 * </ul>
 *
 */
public class JtsWKTReaderShapeParser extends JtsWktShapeParser {

  //Note: Historically, the code here originated from the defunct JtsShapeReadWriter.

  public JtsWKTReaderShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
  }

  @Override
  public Shape parseIfSupported(String wktString) throws ParseException {
    return parseIfSupported(wktString, new WKTReader(ctx.getGeometryFactory()));
  }

  /**
   * Reads WKT from the {@code str} via JTS's {@link com.vividsolutions.jts.io.WKTReader}.
   * @param str
   * @param reader <pre>new WKTReader(ctx.getGeometryFactory()))</pre>
   * @return Non-Null
   */
  protected Shape parseIfSupported(String str, WKTReader reader) throws ParseException {
    try {
      Geometry geom = reader.read(str);

      //Normalizes & verifies coordinates
      checkCoordinates(geom);

      if (geom instanceof com.vividsolutions.jts.geom.Point) {
        com.vividsolutions.jts.geom.Point ptGeom = (com.vividsolutions.jts.geom.Point) geom;
        if (ctx.useJtsPoint())
          return new JtsPoint(ptGeom, ctx);
        else
          return ctx.makePoint(ptGeom.getX(), ptGeom.getY());
      } else if (geom.isRectangle()) {
        return super.makeRectFromPoly(geom);
      } else {
        return super.makeShapeFromGeometry(geom);
      }
    } catch (InvalidShapeException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidShapeException("error reading WKT: "+e.toString(), e);
    }
  }


  protected void checkCoordinates(Geometry geom) {
    // note: JTS WKTReader has already normalized coords with the JTS PrecisionModel.
    geom.apply(new CoordinateSequenceFilter() {
      boolean changed = false;
      @Override
      public void filter(CoordinateSequence seq, int i) {
        double x = seq.getX(i);
        double y = seq.getY(i);

        //Note: we don't simply call ctx.normX & normY because
        //  those methods use the precisionModel, but WKTReader already
        //  used the precisionModel. It's be nice to turn that off somehow but alas.
        if (ctx.isGeo() && ctx.isNormWrapLongitude()) {
          double xNorm = DistanceUtils.normLonDEG(x);
          if (Double.compare(x, xNorm) != 0) {//handles NaN
            changed = true;
            seq.setOrdinate(i, CoordinateSequence.X, xNorm);
          }
//          double yNorm = DistanceUtils.normLatDEG(y);
//          if (y != yNorm) {
//            changed = true;
//            seq.setOrdinate(i,CoordinateSequence.Y,yNorm);
//          }
        }
        ctx.verifyX(x);
        ctx.verifyY(y);
      }

      @Override
      public boolean isDone() { return false; }

      @Override
      public boolean isGeometryChanged() { return changed; }
    });
  }
}
