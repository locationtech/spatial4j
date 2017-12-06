/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
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

public class GeometryAsGeoJSONSerializer extends JsonSerializer<Geometry>
{
  // --------------------------------------------------------------
  // Write JTS To GeoJSON
  // --------------------------------------------------------------

  protected void write(JsonGenerator gen, Coordinate coord) throws IOException {
    gen.writeStartArray();
    gen.writeNumber(coord.x);
    gen.writeNumber(coord.y);
    gen.writeEndArray();
  }

  protected void write(JsonGenerator gen, CoordinateSequence coordseq) throws IOException {
    gen.writeStartArray();
    int dim = coordseq.getDimension();
    for (int i = 0; i < coordseq.size(); i++) {
      gen.writeStartArray();
      gen.writeNumber(coordseq.getOrdinate(i, 0));
      gen.writeNumber(coordseq.getOrdinate(i, 1));
      if (dim > 2) {
        double v = coordseq.getOrdinate(i, 2);
        if (!Double.isNaN(v)) {
          gen.writeNumber(v);
        }
      }
      gen.writeEndArray();
    }
    gen.writeEndArray();
  }

  protected void write(JsonGenerator gen, Coordinate[] coord) throws IOException {
    gen.writeStartArray();
    for (int i = 0; i < coord.length; i++) {
      write(gen, coord[i]);
    }
    gen.writeEndArray();
  }

  protected void write(JsonGenerator gen, Polygon p) throws IOException {
    gen.writeStartArray();
    write(gen, p.getExteriorRing().getCoordinateSequence());
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      write(gen, p.getInteriorRingN(i).getCoordinateSequence());
    }
    gen.writeEndArray();
  }

  @Override
  public void serialize(Geometry geom, JsonGenerator gen, SerializerProvider serializers) 
      throws IOException, JsonProcessingException 
  {
    gen.writeStartObject();
    gen.writeFieldName("type");
    gen.writeString(geom.getClass().getSimpleName());
    
    if (geom instanceof Point) {
      Point v = (Point) geom;
      gen.writeFieldName("coordinates");
      write(gen, v.getCoordinate());
    } else if (geom instanceof Polygon) {
      gen.writeFieldName("coordinates");
      write(gen, (Polygon) geom);
    } else if (geom instanceof LineString) {
      LineString v = (LineString) geom;
      gen.writeFieldName("coordinates");
      write(gen, v.getCoordinateSequence());
    } else if (geom instanceof MultiPoint) {
      MultiPoint v = (MultiPoint) geom;
      gen.writeFieldName("coordinates");
      write(gen, v.getCoordinates());
      return;
    } else if (geom instanceof MultiLineString) {
      MultiLineString v = (MultiLineString) geom;
      gen.writeFieldName("coordinates");
      gen.writeStartArray();
      for (int i = 0; i < v.getNumGeometries(); i++) {
        write(gen, v.getGeometryN(i).getCoordinates());
      }
      gen.writeEndArray();
    } else if (geom instanceof MultiPolygon) {
      MultiPolygon v = (MultiPolygon) geom;
      gen.writeFieldName("coordinates");
      gen.writeStartArray();
      for (int i = 0; i < v.getNumGeometries(); i++) {
        write(gen, (Polygon) v.getGeometryN(i));
      }
      gen.writeEndArray();
    } else if (geom instanceof GeometryCollection) {
      GeometryCollection v = (GeometryCollection) geom;
      gen.writeFieldName("geometries");
      gen.writeStartArray();
      for (int i = 0; i < v.getNumGeometries(); i++) {
        serialize(v.getGeometryN(i), gen, serializers);
      }
      gen.writeEndArray();
    } else {
      throw new UnsupportedOperationException("unknown: " + geom);
    }
    gen.writeEndObject();
  }
}
