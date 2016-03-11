/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import java.util.Iterator;

import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.impl.BufferedLine;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;
import org.locationtech.spatial4j.shape.impl.GeoCircle;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ShapeAsGeoJSONSerializer extends JsonSerializer<Shape>
{
  static final String BUFFER = "buffer";
  static final String BUFFER_UNITS = "buffer_units";
  
  final GeometryAsGeoJSONSerializer forJTS = new GeometryAsGeoJSONSerializer();
  

  protected void write(JsonGenerator gen, double... coords) throws IOException {
    gen.writeStartArray();
    for (int i = 0; i < coords.length; i++) {
      gen.writeNumber(coords[i]);
    }
    gen.writeEndArray();
  }

  
  // Keep 6 decimal places
  public static double round(double v) {
    return Math.round(v * 1000000d) / 1000000d;
  }
  
  /**
   * Helper method to encode a distance property (with optional unit). 
   * <p>
   *  The distance unit is only encoded when <tt>isGeo</tt> is true, and it is converted to km. 
   * </p>
   * <p>
   *  The distance unit is encoded within a properties object.
   * </p>
   * @param output The writer.
   * @param nf The number format.
   * @param dist The distance value to encode.
   * @param isGeo The flag determining 
   * @param distProperty The distance property name.
   * @param distUnitsProperty The distance unit property name.
   */
  void writeDistance(JsonGenerator gen, double dist, boolean isGeo, String distProperty, String distUnitsProperty) 
      throws IOException {
    gen.writeFieldName(distProperty);
    if (isGeo) {
      double distKm = DistanceUtils.degrees2Dist(dist, DistanceUtils.EARTH_MEAN_RADIUS_KM);
      gen.writeNumber( round(distKm) );
      gen.writeFieldName("properties");
      gen.writeStartObject();
      gen.writeFieldName(distUnitsProperty);
      gen.writeString("km");
      gen.writeEndObject();
    } else {
      gen.writeNumber(dist);
    }
  }

  public void write(JsonGenerator gen, Shape shape) throws IOException {
    if (shape == null) {
      throw new NullPointerException("Shape can not be null");
    }
    if (shape instanceof Point) {
      Point v = (Point) shape;
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("Point");
      gen.writeFieldName("coordinates");
      write(gen, v.getX(), v.getY());
      gen.writeEndObject();
      return;
    }
    if (shape instanceof Rectangle) {
      Rectangle v = (Rectangle) shape;
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("Polygon");
      gen.writeFieldName("coordinates");
      gen.writeStartArray();
      gen.writeStartArray();
      write(gen, v.getMinX(), v.getMinY());
      write(gen, v.getMinX(), v.getMaxY());
      write(gen, v.getMaxX(), v.getMaxY());
      write(gen, v.getMaxX(), v.getMinY());
      write(gen, v.getMinX(), v.getMinY());
      gen.writeEndArray();
      gen.writeEndArray();
      gen.writeEndObject();
      return;
    }
    if (shape instanceof BufferedLine) {
      BufferedLine v = (BufferedLine) shape;
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("LineString");
      gen.writeFieldName("coordinates");
      gen.writeStartArray();
      write(gen, v.getA().getX(), v.getA().getY());
      write(gen, v.getB().getX(), v.getB().getY());
      gen.writeEndArray();
      if (v.getBuf() > 0) {
        writeDistance(gen, v.getBuf(), shape.getContext().isGeo(), BUFFER, BUFFER_UNITS);
      }
      gen.writeEndObject();
      return;
    }
    if (shape instanceof BufferedLineString) {
      BufferedLineString v = (BufferedLineString) shape;
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("LineString");
      gen.writeFieldName("coordinates");
      gen.writeStartArray();
      BufferedLine last = null;
      Iterator<BufferedLine> iter = v.getSegments().iterator();
      while (iter.hasNext()) {
        BufferedLine seg = iter.next();
        write(gen, seg.getA().getX(), seg.getA().getY());
        last = seg;
      }
      if (last != null) {
        write(gen, last.getB().getX(), last.getB().getY());
      }
      gen.writeEndArray();
      if (v.getBuf() > 0) {
        writeDistance(gen, v.getBuf(), shape.getContext().isGeo(), BUFFER, BUFFER_UNITS);
      }
      gen.writeEndObject();
      return;
    }
    if (shape instanceof Circle) {
      // See: https://github.com/geojson/geojson-spec/wiki/Proposal---Circles-and-Ellipses-Geoms
      Circle v = (Circle) shape;
      Point center = v.getCenter();
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("Circle");
      gen.writeFieldName("coordinates");
      write(gen, center.getX(), center.getY());
      writeDistance(gen, v.getRadius(), v instanceof GeoCircle, "radius", "radius_units");
      gen.writeEndObject();
      return;
    }
    if (shape instanceof ShapeCollection) {
      ShapeCollection v = (ShapeCollection) shape;
      gen.writeStartObject();
      gen.writeFieldName("type");
      gen.writeString("GeometryCollection");
      gen.writeFieldName("geometries");
      gen.writeStartArray();
      for (int i = 0; i < v.size(); i++) {
        write(gen, v.get(i));
      }
      gen.writeEndArray();
      gen.writeEndObject();
      return;
    }

    // Write the unknown geometry to WKT
    gen.writeStartObject();
    gen.writeFieldName("type");
    gen.writeString("Unknown");
    gen.writeFieldName("wkt");
    gen.writeString(shape.getContext().getFormats().getWktWriter().toString(shape));
    gen.writeEndObject();
  }
  
  
  @Override
  public void serialize(Shape shape, JsonGenerator gen,
      SerializerProvider serializers) throws IOException,
      JsonProcessingException {

    // Use the specialized class
    if (shape instanceof JtsGeometry) {
      forJTS.serialize(((JtsGeometry) shape).getGeom(), gen, serializers);
      return;
    }
    
    write(gen, shape);
  }
}