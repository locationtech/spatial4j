/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.*;
import com.spatial4j.core.shape.impl.BufferedLine;
import com.spatial4j.core.shape.impl.BufferedLineString;
import com.spatial4j.core.shape.impl.GeoCircle;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Iterator;

public class GeoJSONWriter implements ShapeWriter {

  public GeoJSONWriter(SpatialContext ctx, SpatialContextFactory factory) {

  }

  @Override
  public String getFormatName() {
    return ShapeIO.GeoJSON;
  }

  protected void write(Writer output, NumberFormat nf, double... coords) throws IOException {
    output.write('[');
    for (int i = 0; i < coords.length; i++) {
      if (i > 0) {
        output.append(',');
      }
      output.append(nf.format(coords[i]));
    }
    output.write(']');
  }

  @Override
  public void write(Writer output, Shape shape) throws IOException {
    if (shape == null) {
      throw new NullPointerException("Shape can not be null");
    }
    NumberFormat nf = LegacyShapeWriter.makeNumberFormat(6);
    if (shape instanceof Point) {
      Point v = (Point) shape;
      output.append("{\"type\":\"Point\",\"coordinates\":");
      write(output, nf, v.getX(), v.getY());
      output.append('}');
      return;
    }
    if (shape instanceof Rectangle) {
      Rectangle v = (Rectangle) shape;
      output.append("{\"type\":\"Polygon\",\"coordinates\": [[");
      write(output, nf, v.getMinX(), v.getMinY());
      output.append(',');
      write(output, nf, v.getMinX(), v.getMaxY());
      output.append(',');
      write(output, nf, v.getMaxX(), v.getMaxY());
      output.append(',');
      write(output, nf, v.getMaxX(), v.getMinY());
      output.append(',');
      write(output, nf, v.getMinX(), v.getMinY());
      output.append("]]}");
      return;
    }
    if (shape instanceof BufferedLine) {
      BufferedLine v = (BufferedLine) shape;
      output.append("{\"type\":\"LineString\",\"coordinates\": [");
      write(output, nf, v.getA().getX(), v.getA().getY());
      output.append(',');
      write(output, nf, v.getB().getX(), v.getB().getY());
      output.append(',');
      output.append("]");
      if (v.getBuf() > 0) {
        output.append(',');
        output.append("\"buffer\":");
        output.append(nf.format(v.getBuf()));
      }
      output.append('}');
      return;
    }
    if (shape instanceof BufferedLineString) {
      BufferedLineString v = (BufferedLineString) shape;
      output.append("{\"type\":\"LineString\",\"coordinates\": [");
      BufferedLine last = null;
      Iterator<BufferedLine> iter = v.getSegments().iterator();
      while (iter.hasNext()) {
        BufferedLine seg = iter.next();
        if (last != null) {
          output.append(',');
        }
        write(output, nf, seg.getA().getX(), seg.getA().getY());
        last = seg;
      }
      if (last != null) {
        output.append(',');
        write(output, nf, last.getB().getX(), last.getB().getY());
      }
      output.append("]");
      if (v.getBuf() > 0) {
        output.append(',');
        output.append("\"buffer\":");
        output.append(nf.format(v.getBuf()));
      }
      output.append('}');
      return;
    }
    if (shape instanceof Circle) {
      // See: https://github.com/geojson/geojson-spec/wiki/Proposal---Circles-and-Ellipses-Geoms
      Circle v = (Circle) shape;
      Point center = v.getCenter();
      output.append("{\"type\":\"Circle\",\"coordinates\":");
      write(output, nf, center.getX(), center.getY());
      output.append(",\"radius\":");
      if (v instanceof GeoCircle) {
        double distKm =
            DistanceUtils.degrees2Dist(v.getRadius(), DistanceUtils.EARTH_MEAN_RADIUS_KM);
        output.append(nf.format(distKm));
        output.append(",\"properties\":{");
        output.append("\"radius_units\":\"km\"}}");
      } else {
        output.append(nf.format(v.getRadius())).append('}');
      }
      return;
    }
    if (shape instanceof ShapeCollection) {
      ShapeCollection v = (ShapeCollection) shape;
      output.append("{\"type\":\"GeometryCollection\",\"geometries\": [");
      for (int i = 0; i < v.size(); i++) {
        if (i > 0) {
          output.append(',');
        }
        write(output, v.get(i));
      }
      output.append("]}");
      return;
    }
    output.append("{\"type\":\"Unknown\",\"wkt\":\"");
    output.append(LegacyShapeWriter.writeShape(shape));
    output.append("\"}");
  }

  @Override
  public String toString(Shape shape) {
    try {
      StringWriter buffer = new StringWriter();
      write(buffer, shape);
      return buffer.toString();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
