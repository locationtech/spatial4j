/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Locale;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;

/**
 * Writes a shape in the old format.
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
public class LegacyShapeWriter implements ShapeWriter {

  final SpatialContext ctx;

  public LegacyShapeWriter(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
  }

  /**
   * Writes a shape to a String, in a format that can be read by
   * {@link LegacyShapeReader#readShapeOrNull(String, SpatialContext)}
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
    nf.setMinimumFractionDigits(0);
    return nf;
  }

  @Override
  public String getFormatName() {
    return ShapeIO.LEGACY;
  }

  @Override
  public void write(Writer output, Shape shape) throws IOException {
    output.append(writeShape(shape));
  }

  @Override
  public String toString(Shape shape) {
    return writeShape(shape);
  }
}
