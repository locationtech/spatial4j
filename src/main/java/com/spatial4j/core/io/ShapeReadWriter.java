package com.spatial4j.core.io;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Reads and writes {@link Shape}s to strings.
 */
public class ShapeReadWriter<CTX extends SpatialContext> {
  protected CTX ctx;

  @SuppressWarnings("unchecked")
  public ShapeReadWriter(SpatialContext ctx) {
    this.ctx = (CTX) ctx;
  }

  /**
   * Reads a shape from a given string (ie, X Y, XMin XMax... WKT)
   * <ul>
   *   <li>Point: X Y
   *   <br /> 1.23 4.56
   *   </li>
   *   <li>BOX: XMin YMin XMax YMax
   *   <br /> 1.23 4.56 7.87 4.56</li>
   *   <li><a href="http://en.wikipedia.org/wiki/Well-known_text">
   *     WKT (Well Known Text)</a>
   *   <br /> POLYGON( ... )
   *   <br /> <b>Note:</b>Polygons and WKT might not be supported by this
   *   spatial context; you'll have to use {@link com.spatial4j.core.context.jts.JtsSpatialContext}.
   *   </li>
   * </ul>
   * @param value A string representation of the shape; not null.
   * @return A Shape; not null.
   *
   * @see #writeShape
   */
  public Shape readShape(String value) throws InvalidShapeException {
    Shape s = readStandardShape(value);
    if(s == null) {
      throw new InvalidShapeException("Unable to read: "+value);
    }
    return s;
  }

  /**
   * Writes a shape to a String, in a format that can be read by {@link #readShape(String)}.
   * @param shape Not null.
   * @return Not null.
   */
  public String writeShape(Shape shape) {
    return writeShape(shape,makeNumberFormat(6));
  }

  /** Overloaded to provide a number format. */
  public String writeShape(Shape shape, NumberFormat nf) {
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

  protected Shape readStandardShape(String str) {
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
            pt = readLatCommaLonPoint(token);
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
      return null;
    }

    if (str.indexOf(',') != -1)
      return readLatCommaLonPoint(str);
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
  private Point readLatCommaLonPoint(String value) throws InvalidShapeException {
    String[] parts = value.split(",");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Found value [" + value + "] expected two comma separated elements");
    }
    return ctx.makePoint(Double.valueOf(parts[1]), Double.valueOf(parts[0]));
  }
}
