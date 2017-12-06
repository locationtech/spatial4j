/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jts;

import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsPoint;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.text.ParseException;

/**
 * This is an extension of Spatial4j's {@link org.locationtech.spatial4j.io.WKTReader} that processes the entire
 * string with JTS's {@link org.locationtech.jts.io.WKTReader}.  Some differences:
 * <ul>
 *   <li>No support for ENVELOPE and BUFFER</li>
 *   <li>MULTI* shapes use JTS's {@link org.locationtech.jts.geom.GeometryCollection} subclasses,
 *   not {@link org.locationtech.spatial4j.shape.ShapeCollection}</li>
 *   <li>'Z' coordinates are saved into the geometry</li>
 * </ul>
 *
 */
@Deprecated
public class JtsWKTReaderShapeParser extends org.locationtech.spatial4j.io.WKTReader {

  //Note: Historically, the code here originated from the defunct JtsShapeReadWriter.

  public JtsWKTReaderShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
  }

  @Override
  public Shape parseIfSupported(String wktString) throws ParseException {
    return parseIfSupported(wktString, new WKTReader(getShapeFactory().getGeometryFactory()));
  }

  private JtsShapeFactory getShapeFactory() {
    return ((JtsShapeFactory)shapeFactory);
  }

  /**
   * Reads WKT from the {@code str} via JTS's {@link org.locationtech.jts.io.WKTReader}.
   * @param str
   * @param reader <pre>new WKTReader(ctx.getGeometryFactory()))</pre>
   * @return Non-Null
   */
  protected Shape parseIfSupported(String str, WKTReader reader) throws ParseException {
    try {
      Geometry geom = reader.read(str);

      //Normalizes & verifies coordinates
      checkCoordinates(geom);

      if (geom instanceof org.locationtech.jts.geom.Point) {
        org.locationtech.jts.geom.Point ptGeom = (org.locationtech.jts.geom.Point) geom;
        if (getShapeFactory().useJtsPoint())
          return new JtsPoint(ptGeom, (JtsSpatialContext) ctx);
        else
          return getShapeFactory().pointXY(ptGeom.getX(), ptGeom.getY());
      } else if (geom.isRectangle()) {
        return getShapeFactory().makeRectFromRectangularPoly(geom);
      } else {
        return getShapeFactory().makeShapeFromGeometry(geom);
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
