/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.noggit.JSONParser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class GeoJSONReader implements ShapeReader {

  protected static final String BUFFER = "buffer";
  protected static final String BUFFER_UNITS = "buffer_units";

  protected final SpatialContext ctx;
  protected final ShapeFactory shapeFactory;

  public GeoJSONReader(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
    this.shapeFactory = ctx.getShapeFactory();
  }

  @Override
  public String getFormatName() {
    return ShapeIO.GeoJSON;
  }

  @Override
  public final Shape read(Reader reader) throws IOException, ParseException {
    return readShape(new JSONParser(reader));
  }

  @Override
  public Shape read(Object value) throws IOException, ParseException, InvalidShapeException {
    String v = value.toString().trim();
    return read(new StringReader(v));
  }

  @Override
  public Shape readIfSupported(Object value) throws InvalidShapeException {
    String v = value.toString().trim();
    if (!(v.startsWith("{") && v.endsWith("}"))) {
      return null;
    }
    try {
      return read(new StringReader(v));
    } catch (IOException ex) {
    } catch (ParseException e) {
    }
    return null;
  }

  // --------------------------------------------------------------
  // Read GeoJSON
  // --------------------------------------------------------------


  protected void readCoordXYZ(JSONParser parser, ShapeFactory.PointsBuilder pointsBuilder) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    double x = Double.NaN, y = Double.NaN, z = Double.NaN;
    int idx = 0;

    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          double value = parser.getDouble();
          switch(idx) {
            case 0: x = value; break;
            case 1: y = value; break;
            case 2: z = value; break;
          }
          idx++;
          break;

        case JSONParser.ARRAY_END:
          if (idx <= 2) { // don't have a 'z'
            pointsBuilder.pointXY(shapeFactory.normX(x), shapeFactory.normY(y));
          } else {
            pointsBuilder.pointXYZ(shapeFactory.normX(x), shapeFactory.normY(y), shapeFactory.normZ(z));
          }
          return;

        case JSONParser.STRING:
        case JSONParser.BOOLEAN:
        case JSONParser.NULL:
        case JSONParser.OBJECT_START:
        case JSONParser.OBJECT_END:
        case JSONParser.ARRAY_START:
        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return;
  }

  protected void readCoordListXYZ(JSONParser parser, ShapeFactory.PointsBuilder pointsBuilder) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          readCoordXYZ(parser, pointsBuilder); // reads until ARRAY_END
          break;

        case JSONParser.ARRAY_END:
          return;

        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
  }

  protected void readUntilEvent(JSONParser parser, final int event) throws IOException {
    int evt = parser.lastEvent();
    while (true) {
      if (evt == event || evt == JSONParser.EOF) {
        return;
      }
      evt = parser.nextEvent();
    }
  }

  protected Shape readPoint(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    OnePointsBuilder onePointsBuilder = new OnePointsBuilder(shapeFactory);
    readCoordXYZ(parser, onePointsBuilder);
    Point point = onePointsBuilder.getPoint();
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return point;
  }

  protected Shape readLineString(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    ShapeFactory.LineStringBuilder builder = shapeFactory.lineString();
    readCoordListXYZ(parser, builder);

    // check for buffer field
    builder.buffer(readDistance(BUFFER, BUFFER_UNITS, parser));

    Shape out = builder.build();
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return out;
  }

  protected Circle readCircle(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    OnePointsBuilder onePointsBuilder = new OnePointsBuilder(shapeFactory);
    readCoordXYZ(parser, onePointsBuilder);
    Point point = onePointsBuilder.getPoint();

    return shapeFactory.circle(point, readDistance("radius", "radius_units", parser));
  }

  /**
   * Helper method to read a up until a distance value (radius, buffer) and it's corresponding unit are found.
   * <p>
   * This method returns 0 if no distance value is found. This method currently only handles distance units of "km".
   * </p>
   * @param distProperty The name of the property containing the distance value.
   * @param distUnitsProperty The name of the property containing the distance unit. 
   */
  protected double readDistance(String distProperty, String distUnitsProperty, JSONParser parser) throws IOException {
    double dist = 0;

    String key = null;

    int event = JSONParser.OBJECT_END;
    int evt = parser.lastEvent();
    while (true) {
      if (evt == event || evt == JSONParser.EOF) {
        break;
      }
      evt = parser.nextEvent();
      if(parser.wasKey()) {
        key = parser.getString();
      }
      else if(evt==JSONParser.NUMBER || evt==JSONParser.LONG) {
        if(distProperty.equals(key)) {
          dist = parser.getDouble();
        }
      }
      else if(evt==JSONParser.STRING) {
        if(distUnitsProperty.equals(key)) {
          String units = parser.getString();
          //TODO: support for more units?
          if("km".equals(units)) {
            // Convert KM to degrees
            dist =
                DistanceUtils.dist2Degrees(dist, DistanceUtils.EARTH_MEAN_RADIUS_KM);
          }
        }
      }
    }

    return shapeFactory.normDist(dist);
  }

  protected Shape readShape(JSONParser parser) throws IOException, ParseException {
    String type = null;

    String key = null;
    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.STRING:
          if (parser.wasKey()) {
            key = parser.getString();
          } else {
            if ("type".equals(key)) {
              type = parser.getString();
            } else {
              throw new ParseException("Unexpected String Value for key: " + key,
                      (int) parser.getPosition());
            }
          }
          break;

        case JSONParser.ARRAY_START:
          if ("coordinates".equals(key)) {
            Shape shape = readShapeFromCoordinates(type, parser);
            readUntilEvent(parser, JSONParser.OBJECT_END);
            return shape;
          } else if ("geometries".equals(key)) {
            List<Shape> shapes = new ArrayList<Shape>();
            int sub = parser.nextEvent();
            while (sub != JSONParser.EOF) {
              if (sub == JSONParser.OBJECT_START) {
                Shape s = readShape(parser);
                if (s != null) {
                  shapes.add(s);
                }
              } else if (sub == JSONParser.OBJECT_END) {
                break;
              }
              sub = parser.nextEvent();
            }
            if (shapes.isEmpty()) {
              throw new ParseException("Shape Collection with no geometries!",
                  (int) parser.getPosition());
            }
            return ctx.makeCollection(shapes);
          }
          else {
            throw new ParseException("Unknown type: "+type,
                (int) parser.getPosition());
          }

        case JSONParser.ARRAY_END:
          break;

        case JSONParser.OBJECT_START:
          if (key != null) {
           // System.out.println("Unexpected object: " + key);
          }
          break;

        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
        case JSONParser.BOOLEAN:
        case JSONParser.NULL:
        case JSONParser.OBJECT_END:
         // System.out.println(">>>>>" + JSONParser.getEventString(evt) + " :: " + key);
          break;

        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    throw new RuntimeException("unable to parse shape");
  }

  protected Shape readShapeFromCoordinates(String type, JSONParser parser) throws IOException, ParseException {
    switch(type) {
      case "Point":
        return readPoint(parser);
      case "LineString":
        return readLineString(parser);
      case "Circle":
        return readCircle(parser);
      case "Polygon":
        return readPolygon(parser, shapeFactory.polygon()).buildOrRect();
      case "MultiPoint":
        return readMultiPoint(parser);
      case "MultiLineString":
        return readMultiLineString(parser);
      case "MultiPolygon":
        return readMultiPolygon(parser);
      default:
        throw new ParseException("Unable to make shape type: " + type,
                (int) parser.getPosition());
    }
  }

  protected ShapeFactory.PolygonBuilder readPolygon(JSONParser parser, ShapeFactory.PolygonBuilder polygonBuilder) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    boolean firstRing = true;
    int evt = parser.nextEvent();
    while (true) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          if (firstRing) {
            readCoordListXYZ(parser, polygonBuilder);
            firstRing = false;
          } else {
            ShapeFactory.PolygonBuilder.HoleBuilder holeBuilder = polygonBuilder.hole();
            readCoordListXYZ(parser, holeBuilder);
            holeBuilder.endHole();
          }
          break;
        case JSONParser.ARRAY_END:
          return polygonBuilder;
        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
                  (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
  }

  protected Shape readMultiPoint(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    ShapeFactory.MultiPointBuilder builder = shapeFactory.multiPoint();
    readCoordListXYZ(parser, builder);
    return builder.build();
  }

  protected Shape readMultiLineString(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    // TODO need Spatial4j LineString interface
    ShapeFactory.MultiLineStringBuilder builder = shapeFactory.multiLineString();
    int evt = parser.nextEvent();
    while (true) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          ShapeFactory.LineStringBuilder lineStringBuilder = builder.lineString();
          readCoordListXYZ(parser, lineStringBuilder);
          builder.add(lineStringBuilder);
          break;
        case JSONParser.ARRAY_END:
          return builder.build();
        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
                  (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
  }

  protected Shape readMultiPolygon(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    // TODO need Spatial4j Polygon interface
    ShapeFactory.MultiPolygonBuilder builder = shapeFactory.multiPolygon();
    int evt = parser.nextEvent();
    while (true) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          ShapeFactory.PolygonBuilder polygonBuilder = readPolygon(parser, builder.polygon());
          builder.add(polygonBuilder);
          break;
        case JSONParser.ARRAY_END:
          return builder.build();
        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
                  (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
  }
}
