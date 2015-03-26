/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.noggit.JSONParser;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.impl.BufferedLine;
import com.spatial4j.core.shape.impl.BufferedLineString;
import com.spatial4j.core.shape.impl.GeoCircle;



public class GeoJSONFormat extends BaseFormat {
  public static final String FORMAT = "GeoJSON";
  
  public GeoJSONFormat(SpatialContext ctx, SpatialContextFactory factory) {
    super(ctx);
  }
  
  @Override
  public String getFormatName() {
    return FORMAT;
  }
  
  @Override
  public final Shape read(Reader reader) throws IOException, ParseException {
    return readShape(new JSONParser(reader));
  }

  @Override
  public boolean formatMatchs(String v) {
    return v.startsWith("{")&&v.endsWith("}");
  }
  
  //--------------------------------------------------------------
  // Read GeoJSON
  //--------------------------------------------------------------
  
  public List<?> readCoordinates(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);
    
    Deque<List> stack = new ArrayDeque<List>();
    stack.push(new ArrayList());
    int depth = 1;
    
    while( true ) {
      int evt = parser.nextEvent();
      switch(evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          stack.peek().add( parser.getDouble() );
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
          throw new ParseException("Unexpected "+JSONParser.getEventString(evt), (int)parser.getPosition());
      }
    }
  }
  
  public double[] readCoordXY(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);
    
    double[] coord = new double[3];
    int idx = 0;
    
    int evt = parser.nextEvent();
    while( evt != JSONParser.EOF ) {
      switch(evt) {
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
          throw new ParseException("Unexpected "+JSONParser.getEventString(evt), (int)parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return coord;
  }

  public List<double[]> readCoordListXY(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);
    
    List<double[]> coords = new ArrayList<double[]>();
    int evt = parser.nextEvent();
    while( evt != JSONParser.EOF ) {
      switch(evt) {
        case JSONParser.ARRAY_START:
          coords.add(readCoordXY(parser));
          break;

        case JSONParser.ARRAY_END:
          return coords;

        default:
          throw new ParseException("Unexpected "+JSONParser.getEventString(evt), (int)parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    return coords;
  }

  protected void readUntilEvent(JSONParser parser, final int event) throws IOException {
    int evt = parser.lastEvent();
    while(true) {
      if (evt==event || evt == JSONParser.EOF) {
        return;
      }
      evt = parser.nextEvent();
    }
  }

  protected Shape readPoint(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);
    double[] coord = readCoordXY(parser);
    Shape v = ctx.makePoint(coord[0], coord[1]);
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return v;
  }

  protected Shape readLineString(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);
    List<double[]> coords = readCoordListXY(parser);
    
    List<Point> points = new ArrayList<Point>(coords.size());
    for(double[] coord : coords) {
      points.add(ctx.makePoint(coord[0], coord[1]));
    }
    Shape out = ctx.makeLineString(points);
    readUntilEvent(parser, JSONParser.OBJECT_END);
    return out;
  }
  
  /**
   * Default implementation just makes a bbox
   * @throws ParseException 
   */
  protected Shape readPolygon(JSONParser parser) throws IOException, ParseException {
    assert(parser.lastEvent()==JSONParser.ARRAY_START);

    double[] min = new double[] {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
    double[] max = new double[] {Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE};
    
    // Just get all coords and expand
    double[] coords = new double[3];
    int idx=0;
    

    int evt = parser.nextEvent();
    while( evt != JSONParser.EOF ) {
     // System.out.println( ">> "+JSONParser.getEventString(evt));
      switch(evt) {
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
          coords[idx] = parser.getDouble();
          if (coords[idx]>max[idx]) {
            max[idx] = coords[idx];
          }
          if (coords[idx]<min[idx]) {
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
    while( evt != JSONParser.EOF ) {
      switch(evt) {
        case JSONParser.STRING:
          if (parser.wasKey()) {
            key = parser.getString();
          }
          else {
            if ("type".equals(key)) {
              type = parser.getString();
            }
            else {
              throw new ParseException("Unexpected String Value for key: "+key, (int)parser.getPosition());
            }
          }
          break;

        case JSONParser.ARRAY_START: 
          if ("coordinates".equals(key)) {
            Shape shape = null;
            if ("Point".equals(type)) {
              shape = readPoint(parser);
            }
            else if ("LineString".equals(type)) {
              shape = readLineString(parser);
            }
            else {
              shape = makeShapeFromCoords(type,
                  readCoordinates(parser));
            }
            if(shape!=null) {
              readUntilEvent(parser, JSONParser.OBJECT_END);
              return shape;
            }
            throw new ParseException("Unable to make shape type: "+type, (int)parser.getPosition());
          }
          else if ("geometries".equals(key)) {
            List<Shape> shapes = new ArrayList<Shape>();
            int sub = parser.nextEvent();
            while(sub!=JSONParser.EOF) {
              if(sub==JSONParser.OBJECT_START) {
                Shape s = readShape(parser);
                if(s!=null) {
                  shapes.add(s);
                }
              }
              else if(sub == JSONParser.OBJECT_END) {
                break;
              }
              sub = parser.nextEvent();
            }
            if(shapes.isEmpty()) {
              throw new ParseException("Shape Collection with now geometries!", (int)parser.getPosition());
            }
            return ctx.makeCollection(shapes);
          }
          break;
          
        case JSONParser.ARRAY_END:
          break;

        case JSONParser.OBJECT_START:
          if(key!=null) {
            System.out.println("Unexpected object: "+key);
          }
          break;
          
        case JSONParser.LONG:
        case JSONParser.NUMBER:
        case JSONParser.BIGNUMBER:
        case JSONParser.BOOLEAN:
        case JSONParser.NULL:
        case JSONParser.OBJECT_END:
          System.out.println(">>>>>"+JSONParser.getEventString(evt)  + " :: " + key);
          break;
          
        default:
          throw new ParseException("Unexpected "+JSONParser.getEventString(evt), (int)parser.getPosition());
      }
      evt = parser.nextEvent();
    }
    throw new RuntimeException("unable to parse shape");
  }
  
  protected Shape makeShapeFromCoords(String type, List coords) {
    return null;  // default is unsupported
  }

  protected void write(Writer output, NumberFormat nf, double ... coords) throws IOException {
    output.write('[');
    for(int i=0;i<coords.length; i++) {
      if (i>0) {
        output.append(',');
      }
      output.append(nf.format(coords[i]));
    }
    output.write(']');
  }
  
  @Override
  public void write(Writer output, Shape shape) throws IOException 
  {
    if (shape==null) {
      throw new NullPointerException("Shape can not be null");
    }
    NumberFormat nf = LegacyShapeReadWriterFormat.makeNumberFormat(6);
    if (shape instanceof Point) {
      Point v = (Point)shape;
      output.append("{\"type\":\"Point\",\"coordinates\":");
      write(output, nf, v.getX(), v.getY());
      output.append('}');
      return;
    }
    if (shape instanceof Rectangle) {
      Rectangle v = (Rectangle)shape;
      output.append("{\"type\":\"Polygon\",\"coordinates\": [[");
      write(output, nf, v.getMinX(), v.getMinY()); output.append(',');
      write(output, nf, v.getMinX(), v.getMaxY()); output.append(',');
      write(output, nf, v.getMaxX(), v.getMaxY()); output.append(',');
      write(output, nf, v.getMaxX(), v.getMinY()); 
      output.append("]]}");
      return;
    }
    if (shape instanceof BufferedLine) {
      BufferedLine v = (BufferedLine)shape;
      output.append("{\"type\":\"LineString\",\"coordinates\": [");
      write(output, nf, v.getA().getX(), v.getA().getY()); output.append(',');
      write(output, nf, v.getB().getX(), v.getB().getY()); output.append(',');
      output.append("]");
      if (v.getBuf()>0) {
        output.append("\"buffer\":");
        output.append(nf.format(v.getBuf()));
      }
      output.append('}');
      return;
    }
    if (shape instanceof BufferedLineString) {
      BufferedLineString v = (BufferedLineString)shape;
      output.append("{\"type\":\"LineString\",\"coordinates\": [");
      BufferedLine last = null;
      Iterator<BufferedLine> iter = v.getSegments().iterator();
      while(iter.hasNext()) {
        BufferedLine seg = iter.next();
        if (last!=null) {
          output.append(',');
        }
        write(output, nf, seg.getA().getX(), seg.getA().getY());
        last = seg;
      }
      if (last!=null) {
        output.append(',');
        write(output, nf, last.getB().getX(), last.getB().getY());
      }
      output.append("]");
      if (v.getBuf()>0) {
        output.append("\"buffer\":");
        output.append(nf.format(v.getBuf()));
      }
      output.append('}');
      return;
    }
    if (shape instanceof Circle) {
      // See: https://github.com/geojson/geojson-spec/wiki/Proposal---Circles-and-Ellipses-Geoms
      Circle v = (Circle)shape;
      Point center = v.getCenter();
      output.append("{\"type\":\"Circle\",\"coordinates\":");
      write(output, nf, center.getX(), center.getY());
      output.append("\"radius\":");
      if (v instanceof GeoCircle) {
        double distKm = DistanceUtils.degrees2Dist(v.getRadius(),  DistanceUtils.EARTH_MEAN_RADIUS_KM);
        output.append(nf.format(distKm));
        output.append(",\"properties\":{");
        output.append(",\"radius_units\":\"km\"}}");
      }
      else {
        output.append(nf.format(v.getRadius())).append('}');
      }
      return;
    }
    if (shape instanceof ShapeCollection) {
      ShapeCollection v = (ShapeCollection)shape;
      output.append("{\"type\":\"GeometryCollection\",\"geometries\": [");
      for(int i=0; i<v.size(); i++) {
        if (i>0) {
          output.append(',');
        }
        write(output, v.get(i));
      }
      output.append("]}");
      return;
    }
    
    output.append("{\"type\":\"Unknown\",\"wkt\":\"");
    output.append( LegacyShapeReadWriterFormat.writeShape(shape));
    output.append("\"}");
  }
}
