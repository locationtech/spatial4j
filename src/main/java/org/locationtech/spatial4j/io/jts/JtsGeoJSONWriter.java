/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jts;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.io.GeoJSONWriter;
import org.locationtech.spatial4j.io.LegacyShapeWriter;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class JtsGeoJSONWriter extends GeoJSONWriter {

  protected final JtsSpatialContext ctx;

  public JtsGeoJSONWriter(JtsSpatialContext ctx, SpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
  }

  // --------------------------------------------------------------
  // Write JTS To GeoJSON
  // --------------------------------------------------------------

  protected void write(Writer output, NumberFormat nf, Coordinate coord) throws IOException {
    output.write('[');
    output.write(nf.format(coord.x));
    output.write(',');
    output.write(nf.format(coord.y));
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, CoordinateSequence coordseq)
      throws IOException {
    output.write('[');
    int dim = coordseq.getDimension();
    for (int i = 0; i < coordseq.size(); i++) {
      if (i > 0) {
        output.write(',');
      }
      output.write('[');
      output.write(nf.format(coordseq.getOrdinate(i, 0)));
      output.write(',');
      output.write(nf.format(coordseq.getOrdinate(i, 1)));
      if (dim > 2) {
        double v = coordseq.getOrdinate(i, 2);
        if (!Double.isNaN(v)) {
          output.write(',');
          output.write(nf.format(v));
        }
      }
      output.write(']');
    }
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, Coordinate[] coord) throws IOException {
    output.write('[');
    for (int i = 0; i < coord.length; i++) {
      if (i > 0) {
        output.append(',');
      }
      write(output, nf, coord[i]);
    }
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, Polygon p) throws IOException {
    output.write('[');
    write(output, nf, p.getExteriorRing().getCoordinateSequence());
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      output.append(',');
      write(output, nf, p.getInteriorRingN(i).getCoordinateSequence());
    }
    output.write(']');
  }

  public static boolean[] flags = new boolean[14];

  private void writeToFile(){
    try
    {
      String filename= "write.txt";
      FileWriter fw = new FileWriter(filename,false); //the true will append the new data
      fw.write("write \n");
      int count = 0;
      for (boolean b :flags) {
        if (b) count ++;
        fw.write(b + " ");
      }
      fw.write("\nCoverage: " + (Double.toString((double) count/flags.length)) );
      fw.close();
    }
    catch(IOException ioe)
    {
      System.err.println("IOException: " + ioe.getMessage());
    }
  }

  public void write(Writer output, Geometry geom) throws IOException {
    NumberFormat nf = LegacyShapeWriter.makeNumberFormat(6);
    if (geom instanceof Point) {
      flags[0] = true;
      Point v = (Point) geom;
      output.append("{\"type\":\"Point\",\"coordinates\":");
      write(output, nf, v.getCoordinate());
      output.append("}");
      writeToFile();
      return;
    } else if (geom instanceof Polygon) {
      flags[1] = true;
      output.append("{\"type\":\"Polygon\",\"coordinates\":");
      write(output, nf, (Polygon) geom);
      output.append("}");
      writeToFile();
      return;
    } else if (geom instanceof LineString) {
      flags[2] = true;
      LineString v = (LineString) geom;
      output.append("{\"type\":\"LineString\",\"coordinates\":");
      write(output, nf, v.getCoordinateSequence());
      output.append("}");
      writeToFile();
      return;
    } else if (geom instanceof MultiPoint) {
      MultiPoint v = (MultiPoint) geom;
      output.append("{\"type\":\"MultiPoint\",\"coordinates\":");
      write(output, nf, v.getCoordinates());
      output.append("}");
      flags[3] = true;
      writeToFile();
      return;
    } else if (geom instanceof MultiLineString) {
      MultiLineString v = (MultiLineString) geom;
      flags[4] = true;
      output.append("{\"type\":\"MultiLineString\",\"coordinates\":[");
      for (int i = 0; i < v.getNumGeometries(); i++) {
        flags[5] = true;
        if (i > 0) {
          flags[6] = true;
          output.append(',');
        }
        write(output, nf, v.getGeometryN(i).getCoordinates());
      }
      output.append("]}");
    } else if (geom instanceof MultiPolygon) {
      flags[7] = true;
      MultiPolygon v = (MultiPolygon) geom;
      output.append("{\"type\":\"MultiPolygon\",\"coordinates\":[");
      for (int i = 0; i < v.getNumGeometries(); i++) {
        flags[8] = true;
        if (i > 0) {
          flags[9] = true;
          output.append(',');
        }
        write(output, nf, (Polygon) v.getGeometryN(i));
      }
      output.append("]}");
    } else if (geom instanceof GeometryCollection) {
      flags[10] = true;
      GeometryCollection v = (GeometryCollection) geom;
      output.append("{\"type\":\"GeometryCollection\",\"geometries\":[");
      for (int i = 0; i < v.getNumGeometries(); i++) {
        flags[11] = true;
        if (i > 0) {
          flags[12] = true;
          output.append(',');
        }
        write(output, v.getGeometryN(i));
      }
      output.append("]}");
    } else {
      flags[13] = true;
      throw new UnsupportedOperationException("unknown: " + geom);
    }
  }

  @Override
  public void write(Writer output, Shape shape) throws IOException {
    if (shape == null) {
      throw new NullPointerException("Shape can not be null");
    }
    if (shape instanceof JtsGeometry) {
      write(output, ((JtsGeometry) shape).getGeom());
      return;
    }
    super.write(output, shape);
  }
}
