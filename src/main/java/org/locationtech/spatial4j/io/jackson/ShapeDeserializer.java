/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import java.io.IOException;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ShapeDeserializer extends JsonDeserializer<Shape>
{
  public final SpatialContext ctx;

  public ShapeDeserializer() {
    this(JtsSpatialContext.GEO);
  }
  
  public ShapeDeserializer(SpatialContext ctx) {
    this.ctx = ctx;
  }
  
  public Point readPoint(ArrayNode arr, ShapeFactory factory) {
    double x = arr.get(0).asDouble();
    double y = arr.get(1).asDouble();
    if(arr.size()==3) {
      double z = arr.get(3).asDouble();
      return factory.pointXYZ(x, y, z);
    }
    return factory.pointXY(x, y);
  }
  
  private void fillPoints( ShapeFactory.PointsBuilder b, ArrayNode arrs ) {
    for(int i=0; i<arrs.size(); i++) {
      ArrayNode arr = (ArrayNode)arrs.get(i);
      
      double x = arr.get(0).asDouble();
      double y = arr.get(1).asDouble();
      if(arr.size()==3) {
        double z = arr.get(3).asDouble();
        b.pointXYZ(x, y, z);
      }
      else {
        b.pointXY(x, y);
      }
    }
  }
  
  private void fillPolygon(ShapeFactory.PolygonBuilder b, ArrayNode arr ) {
    ArrayNode coords = (ArrayNode)arr.get(0);
    for(int i=0; i<coords.size(); i++) {
      ArrayNode n = (ArrayNode)coords.get(i);
      double x = n.get(0).asDouble();
      double y = n.get(1).asDouble();
      if(n.size()==3) {
        double z = n.get(2).asDouble();
        b.pointXYZ(x, y, z);
      }
      else {
        b.pointXY(x, y);
      }
    }
    
    // Now add the holes
    for(int h=1; h<arr.size(); h++) {
      ShapeFactory.PolygonBuilder.HoleBuilder hole = b.hole();
      coords = (ArrayNode)arr.get(h);
      for(int i=0; i<coords.size(); i++) {
        ArrayNode n = (ArrayNode)coords.get(i);
        double x = n.get(0).asDouble();
        double y = n.get(1).asDouble();
        if(n.size()==3) {
          double z = n.get(2).asDouble();
          hole.pointXYZ(x, y, z);
        }
        else {
          hole.pointXY(x, y);
        }
      }
      hole.endHole();
    }
  }

  public Shape read(ObjectNode node, ShapeFactory factory) throws IOException {

    if(!node.has("type")) {
      throw new IllegalArgumentException("Missing 'type'");
    }
    
    String type = node.get("type").asText();
    if(node.has("geometries")) {
      if(!"GeometryCollection".equals(type)) {
        throw new IllegalArgumentException("Geometries are only expected for GeometryCollections");
      }

      ShapeFactory.MultiShapeBuilder<Shape> b = factory.multiShape(Shape.class);
      ArrayNode arr = (ArrayNode)node.get("geometries");
      for(int i=0; i<arr.size(); i++) {
        b.add( read((ObjectNode)arr.get(i), factory ) );
      }
      return b.build();
    }
    
    ObjectNode props = (ObjectNode)node.get("properties");
    ArrayNode arr = (ArrayNode)node.get("coordinates");
    
    
    if("Point".equals(type)) {
      if(props!=null) {
        throw new IllegalArgumentException("we don't support props on points...");
      }
      return readPoint(arr, factory);
    }
    if("MultiPoint".equals(type)) {
      if(props!=null) {
        throw new IllegalArgumentException("we don't support props on points...");
      }
      
      ShapeFactory.MultiPointBuilder b = factory.multiPoint();
      fillPoints(b, arr);
      return b.build();
    }
    
    boolean isMultiLine = "MultiLineString".equals(type);
    if(isMultiLine || "LineString".equals(type)) {
      double buffer = 0;
      if(node.has(ShapeAsGeoJSONSerializer.BUFFER)) {
        buffer = node.get(ShapeAsGeoJSONSerializer.BUFFER).asDouble();
        if(props!=null) {
          if("km".equals(props.get(ShapeAsGeoJSONSerializer.BUFFER_UNITS).asText())) {
            buffer = DistanceUtils.dist2Degrees(buffer, DistanceUtils.EARTH_MEAN_RADIUS_KM);
          }
        }
      }
      if(isMultiLine) {
        ShapeFactory.MultiLineStringBuilder builder = factory.multiLineString();
        for(int i=0; i<arr.size(); i++) {
          ShapeFactory.LineStringBuilder b = builder.lineString();
          fillPoints(b, (ArrayNode)arr.get(i));
          b.buffer(buffer);
          builder.add(b);
        }
        return builder.build();
      }
      
      ShapeFactory.LineStringBuilder builder = factory.lineString();
      fillPoints(builder, arr);
      builder.buffer(buffer);
      return builder.build();
    }
    
    if("Polygon".equals(type)) {
      ShapeFactory.PolygonBuilder b = factory.polygon();
      fillPolygon(b, arr);
      return b.buildOrRect();
    }

    if("MultiPolygon".equals(type)) {
      ShapeFactory.MultiPolygonBuilder buildier = factory.multiPolygon();
      for(int i=0; i<arr.size(); i++) {
        ShapeFactory.PolygonBuilder b = buildier.polygon();
        fillPolygon(b, (ArrayNode)arr.get(i));
        buildier.add(b);
      }
      return buildier.build();
    }

    if("Circle".equals(type)) {
      double radius = 0;
      if(node.has("radius")) {
        radius = node.get("radius").asDouble();
        if(props!=null) {
          if("km".equals(props.get("radius_units").asText())) {
            radius = DistanceUtils.dist2Degrees(radius, DistanceUtils.EARTH_MEAN_RADIUS_KM);
          }
        }
      }
      return factory.circle(readPoint(arr, factory), radius);
    }

    throw new IllegalArgumentException("Unsupported type: "+type);
  }
  
  
  
  public Shape read(JsonParser jp, ShapeFactory factory) throws IOException {
    if(!jp.getCurrentToken().isStructStart()) {
      throw new JsonParseException(jp, "Expect the start of GeoJSON Geometry object");
    }
    
    return read( (ObjectNode)jp.getCodec().readTree(jp), factory );
  }
  
  @Override
  public Shape deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    
    JsonToken t = jp.getCurrentToken();
    if(t.isStructStart()) {
      return read( jp, ctx.getShapeFactory() );
    }
    if (t.isScalarValue()) {
      String txt = t.asString();
      if(txt!=null && txt.length()>0) {
        try {
          return ctx.getFormats().read(txt);
        } catch (Exception e) {
          throw new JsonParseException(jp, "error reading shape", e);
        }
      }
      return null; // empty string
    }
    throw new JsonParseException(jp, "can't read GeoJSON yet");
  }
}