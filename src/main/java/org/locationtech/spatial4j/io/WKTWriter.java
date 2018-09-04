/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.impl.BufferedLine;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;

import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Iterator;

public class WKTWriter implements ShapeWriter {

  @Override
  public String getFormatName() {
    return ShapeIO.WKT;
  }


  protected StringBuilder append(StringBuilder buffer, Point p, NumberFormat nf) {
    return buffer.append(nf.format(p.getX())).append(' ').append( nf.format(p.getY()));
  }

  protected NumberFormat getNumberFormat() {
    return LegacyShapeWriter.makeNumberFormat(6);
  }

  @Override
  public String toString(Shape shape) {
    NumberFormat nf = getNumberFormat();
    if (shape instanceof Point) {
      StringBuilder buffer = new StringBuilder();
      return append(buffer.append("POINT ("),(Point)shape,nf).append(")").toString();
    }
    if (shape instanceof Rectangle) {
      NumberFormat nfMIN = nf;
      NumberFormat nfMAX = LegacyShapeWriter.makeNumberFormat(6);

      nfMIN.setRoundingMode( RoundingMode.FLOOR );
      nfMAX.setRoundingMode( RoundingMode.CEILING );
      
      Rectangle rect = (Rectangle)shape;
      return "ENVELOPE (" +
          // '(' x1 ',' x2 ',' y2 ',' y1 ')'
        nfMIN.format(rect.getMinX()) + ", " + nfMAX.format(rect.getMaxX()) + ", "+
        nfMAX.format(rect.getMaxY()) + ", " + nfMIN.format(rect.getMinY()) + ")";
//      
//      return "POLYGON(( "+
//         nf.format(rect.getMinX()) + " " + nf.format(rect.getMinY()) + ", "+
//         nf.format(rect.getMinX()) + " " + nf.format(rect.getMaxY()) + ", "+
//         nf.format(rect.getMaxX()) + " " + nf.format(rect.getMaxY()) + ", "+
//         nf.format(rect.getMaxX()) + " " + nf.format(rect.getMinY()) + ", "+
//         nf.format(rect.getMinX()) + " " + nf.format(rect.getMinY()) + "))";
    }
    if (shape instanceof Circle) {
      Circle c = (Circle) shape;

      StringBuilder str = new StringBuilder();
      str.append("BUFFER (POINT (")
        .append(nf.format(c.getCenter().getX())).append(" ")
        .append(nf.format(c.getCenter().getY()))
        .append("), ")
        .append(nf.format(c.getRadius()))
        .append(")");
      return str.toString();
    }
    if (shape instanceof BufferedLineString) {
      BufferedLineString line = (BufferedLineString) shape;
      StringBuilder str = new StringBuilder();

      double buf = line.getBuf();
      if (buf > 0d) {
        str.append("BUFFER (");
      }

      str.append("LINESTRING (");
      Iterator<BufferedLine> iter = line.getSegments().iterator();
      while(iter.hasNext()) {
        BufferedLine seg = iter.next();
        append(str,seg.getA(),nf).append(", ");
        if(!iter.hasNext()) {
          append(str,seg.getB(),nf);
        }
      }
      str.append(")");

      if (buf > 0d) {
        str.append(", ").append(nf.format(buf)).append(")");
      }
      return str.toString();
    }
    if(shape instanceof ShapeCollection) {
      StringBuilder buffer = new StringBuilder();
      buffer.append("GEOMETRYCOLLECTION (");
      boolean first = true;
      for(Shape sub : ((ShapeCollection<? extends Shape>)shape).getShapes()) {
        if(!first) {
          buffer.append(",");
        }
        buffer.append(toString(sub));
        first = false;
      }
      buffer.append(")");
      return buffer.toString();
    }
    return LegacyShapeWriter.writeShape(shape, nf);
  }
  
  @Override
  public void write(Writer output, Shape shape) throws IOException {
    output.append( toString(shape) );
  }
}