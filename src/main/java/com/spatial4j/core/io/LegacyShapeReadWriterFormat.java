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

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Reads & writes a shape from a given string in the old format.
 * <ul>
 *   <li>Point: X Y
 *   <br /> 1.23 4.56
 *   </li>
 *   <li>Rect: XMin YMin XMax YMax
 *   <br /> 1.23 4.56 7.87 4.56
 *   </li>
 *   <li>{CIRCLE} '(' {POINT} {DISTANCE} ')' <br/>
 *   CIRCLE is "CIRCLE" or "Circle" (no other case), and POINT is "X Y" order pair of doubles, or
 *   "Y,X" (lat,lon) pair of doubles, and DISTANCE is "d=RADIUS" or "distance=RADIUS" where RADIUS
 *   is a double that is the distance radius in degrees.
 *   </li>
 * </ul>
 */
@Deprecated
public class LegacyShapeReadWriterFormat {

  private LegacyShapeReadWriterFormat() {
  }

  /**
   * Writes a shape to a String, in a format that can be read by
   * {@link #readShapeOrNull(String, com.spatial4j.core.context.SpatialContext)}.
   * @param shape Not null.
   * @return Not null.
   */
  public static String writeShape(Shape shape) {
    return writeShape(shape, makeNumberFormat(6));
  }

  /** Overloaded to provide a number format. */
  public static String writeShape(Shape shape, NumberFormat nf) {
    if (shape instanceof Point) {
      Point point = (Point) shape;
      return nf.format(point.getX()) + " " + nf.format(point.getY());
    }
    else if (shape instanceof Rectangle) {
      Rectangle rect = (Rectangle)shape;
      return
          nf.format(rect.getMinX()) + " " +
              nf.format(rect.getMinY()) + " " +
              nf.format(rect.getMaxX()) + " " +
              nf.format(rect.getMaxY());
    }
    else if (shape instanceof Circle) {
      Circle c = (Circle) shape;
      return "Circle(" +
          nf.format(c.getCenter().getX()) + " " +
          nf.format(c.getCenter().getY()) + " " +
          "d=" + nf.format(c.getRadius()) +
          ")";
    }
    return shape.toString();
  }

  /**
   * A convenience method to create a suitable NumberFormat for writing numbers.
   */
  public static NumberFormat makeNumberFormat(int fractionDigits) {
    NumberFormat nf = NumberFormat.getInstance(Locale.ROOT);//not thread-safe
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(fractionDigits);
    nf.setMinimumFractionDigits(fractionDigits);
    return nf;
  }

  /** Reads the shape specification as defined in the class javadocs. If the first character is
   * a letter but it doesn't complete out "Circle" or "CIRCLE" then this method returns null,
   * offering the caller the opportunity to potentially try additional parsing.
   * If the first character is not a letter then it's assumed to be a point or rectangle. If that
   * doesn't work out then an {@link com.spatial4j.core.exception.InvalidShapeException} is thrown.
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

}
