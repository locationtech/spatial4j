/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.noggit.JSONParser;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

public class GeoJSONReader implements ShapeReader {

  protected static final String BUFFER = "buffer";
  protected static final String BUFFER_UNITS = "buffer_units";

  final SpatialContext ctx;

  public GeoJSONReader(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
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

  public List<?> readCoordinates(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    Deque<List> stack = new ArrayDeque<List>();
    stack.push(new ArrayList());
    int depth = 1;

    while (true) {
      int evt = parser.nextEvent();
      switch (evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          stack.peek().add(parser.getDouble());
          break;

        case JSONParser.ARRAY_START:
          stack.push(new ArrayList());
          depth++;
          break;

        case JSONParser.ARRAY_END:
          depth--;

          List val = stack.pop();
          if (depth == 0) {
            return val;
          }
          stack.peek().add(val);
          break;

        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
    }
  }

  public double[] readCoordXY(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    double[] coord = new double[3];
    int idx = 0;

    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          coord[idx++] = parser.getDouble();
          break;

        case JSONParser.ARRAY_END:
          return coord;

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
    return coord;
  }

  public List<double[]> readCoordListXY(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    List<double[]> coords = new ArrayList<double[]>();
    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      switch (evt) {
        case JSONParser.ARRAY_START:
          coords.add(readCoordXY(parser));
          break;

        case JSONParser.ARRAY_END:
          return coords;

        default:
          throw new ParseException("Unexpected " + JSONParser.getEventString(evt),
              (int) parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return coords;
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
    double[] coord = readCoordXY(parser);
    Shape v = ctx.makePoint(coord[0], coord[1]);
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return v;
  }

  protected Shape readLineString(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    List<double[]> coords = readCoordListXY(parser);

    List<Point> points = new ArrayList<Point>(coords.size());
    for (double[] coord : coords) {
      points.add(ctx.makePoint(coord[0], coord[1]));
    }

    // check for buffer field
    double buf = readDistance(BUFFER, BUFFER_UNITS, parser);

    Shape out = buf == 0d ? ctx.makeLineString(points) : ctx.makeBufferedLineString(points, buf);
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return out;
  }

  protected Circle readCircle(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);
    double[] coord = readCoordXY(parser);
    Point point = ctx.makePoint(coord[0], coord[1]);

    return ctx.makeCircle(point, readDistance("radius", "radius_units", parser));
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

    return dist;
  }

  /**
   * This method takes a polygon and makes a bbox from it
   * 
   * NOTE: not currently used!  polygon is currently implemented in:
   *   {@link GeoJSONReader#makeShapeFromCoords(String, List)}
   *   
   * We could add a 'strict' or 'leinent' mode that would try the best it can
   * 
   * @throws ParseException
   */
  protected Shape readPolygon(JSONParser parser) throws IOException, ParseException {
    assert (parser.lastEvent() == JSONParser.ARRAY_START);

    double[] min = new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    double[] max = new double[] {Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};

    // Just get all coords and expand
    double[] coords = new double[3];
    int idx = 0;


    int evt = parser.nextEvent();
    while (evt != JSONParser.EOF) {
      // System.out.println( ">> "+JSONParser.getEventString(evt));
      switch (evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          coords[idx] = parser.getDouble();
          if (coords[idx] > max[idx]) {
            max[idx] = coords[idx];
          }
          if (coords[idx] < min[idx]) {
            min[idx] = coords[idx];
          }
          idx++;
          break;

        case JSONParser.ARRAY_END:
          idx = 0;
          break;

        case JSONParser.OBJECT_END: {
          return ctx.makeRectangle(min[0], max[0], min[1], max[1]);
        }
      }
      evt = parser.nextEvent();
    }
    throw new RuntimeException("Could not find polygon");
  }

  public Shape readShape(JSONParser parser) throws IOException, ParseException {
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
            Shape shape = null;
            if ("Point".equals(type)) {
              shape = readPoint(parser);
            } else if ("LineString".equals(type)) {
              shape = readLineString(parser);
            } else if ("Circle".equals(type)) {
              shape = readCircle(parser);
            }else {
              shape = makeShapeFromCoords(type, readCoordinates(parser));
            }
            if (shape != null) {
              readUntilEvent(parser, JSONParser.OBJECT_END);
              return shape;
            }
            throw new ParseException("Unable to make shape type: " + type,
                (int) parser.getPosition());
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
              throw new ParseException("Shape Collection with now geometries!",
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

  protected Shape makeShapeFromCoords(String type, List coords) {
    // TODO?: we could default to making a bbox rather than throwing an error
    throw new RuntimeException("Unsupported: " + type); // JTS Supports this
  }
}
