/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;

/**
 * Reads a shape from the old format.
 * <ul>
 *   <li>Point: X Y
 *   <br> 1.23 4.56
 *   </li>
 *   <li>Rect: XMin YMin XMax YMax
 *   <br> 1.23 4.56 7.87 4.56
 *   </li>
 *   <li>{CIRCLE} '(' {POINT} {DISTANCE} ')' <br>
 *   CIRCLE is "CIRCLE" or "Circle" (no other case), and POINT is "X Y" order pair of doubles, or
 *   "Y,X" (lat,lon) pair of doubles, and DISTANCE is "d=RADIUS" or "distance=RADIUS" where RADIUS
 *   is a double that is the distance radius in degrees.
 *   </li>
 * </ul>
 */
@Deprecated
public class LegacyShapeReader implements ShapeReader {

  final SpatialContext ctx;

  public LegacyShapeReader(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
  }


  /** Reads the shape specification as defined in the class javadocs. If the first character is
   * a letter but it doesn't complete out "Circle" or "CIRCLE" then this method returns null,
   * offering the caller the opportunity to potentially try additional parsing.
   * If the first character is not a letter then it's assumed to be a point or rectangle. If that
   * doesn't work out then an {@link org.locationtech.spatial4j.exception.InvalidShapeException} is thrown.
   */
  public static Shape readShapeOrNull(String str, SpatialContext ctx) throws InvalidShapeException {
    if (str == null || str.length() == 0) {
      throw new InvalidShapeException(str);
    }

    if (Character.isLetter(str.charAt(0))) {
      if (str.startsWith("Circle(") || str.startsWith("CIRCLE(")) {
        int idx = str.lastIndexOf(')');
        if (idx > 0) {
          String body = str.substring("Circle(".length(), idx);
          StringTokenizer st = new StringTokenizer(body, " ");
          String token = st.nextToken();
          Point pt;
          if (token.indexOf(',') != -1) {
            pt = readLatCommaLonPoint(token, ctx);
          } else {
            double x = Double.parseDouble(token);
            double y = Double.parseDouble(st.nextToken());
            pt = ctx.makePoint(x, y);
          }
          Double d = null;

          String arg = st.nextToken();
          idx = arg.indexOf('=');
          if (idx > 0) {
            String k = arg.substring(0, idx);
            if (k.equals("d") || k.equals("distance")) {
              d = Double.parseDouble(arg.substring(idx + 1));
            } else {
              throw new InvalidShapeException("unknown arg: " + k + " :: " + str);
            }
          } else {
            d = Double.parseDouble(arg);
          }
          if (st.hasMoreTokens()) {
            throw new InvalidShapeException("Extra arguments: " + st.nextToken() + " :: " + str);
          }
          if (d == null) {
            throw new InvalidShapeException("Missing Distance: " + str);
          }
          //NOTE: we are assuming the units of 'd' is the same as that of the spatial context.
          return ctx.makeCircle(pt, d);
        }
      }
      return null;//caller has opportunity to try other parsing
    }

    if (str.indexOf(',') != -1)
      return readLatCommaLonPoint(str, ctx);
    StringTokenizer st = new StringTokenizer(str, " ");
    double p0 = Double.parseDouble(st.nextToken());
    double p1 = Double.parseDouble(st.nextToken());
    if (st.hasMoreTokens()) {
      double p2 = Double.parseDouble(st.nextToken());
      double p3 = Double.parseDouble(st.nextToken());
      if (st.hasMoreTokens())
        throw new InvalidShapeException("Only 4 numbers supported (rect) but found more: " + str);
      return ctx.makeRectangle(p0, p2, p1, p3);
    }
    return ctx.makePoint(p0, p1);
  }

  /** Reads geospatial latitude then a comma then longitude. */
  private static Point readLatCommaLonPoint(String value, SpatialContext ctx) throws InvalidShapeException {
    double[] latLon = ParseUtils.parseLatitudeLongitude(value);
    return ctx.makePoint(latLon[1], latLon[0]);
  }
  
  //-------

  @Override
  public String getFormatName() {
    return ShapeIO.LEGACY;
  }

  @Override
  public Shape read(Object value) throws IOException, ParseException, InvalidShapeException {
    Shape shape = readShapeOrNull(value.toString(), ctx);
    if(shape==null) {
      throw new ParseException("unable to read shape: "+value, 0);
    }
    return readShapeOrNull(value.toString(), ctx);
  }

  @Override
  public Shape readIfSupported(Object value) throws InvalidShapeException {
    return readShapeOrNull(value.toString(), ctx);
  }

  @Override
  public Shape read(Reader reader) throws IOException, ParseException, InvalidShapeException {
    return read(WKTReader.readString(reader));
  }
}
