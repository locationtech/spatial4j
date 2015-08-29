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
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;



public class PolyshapeReader implements ShapeReader {
  final SpatialContext ctx;

  public PolyshapeReader(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
  }

  @Override
  public String getFormatName() {
    return ShapeIO.POLY;
  }

  /**
   * Subclass may try to make multiple points into a MultiPoint
   */
  protected Shape makeCollection(List<Shape> shapes) {
    return ctx.makeCollection(shapes);
  }
  
  @Override
  public Shape read(Object value) throws IOException, ParseException, InvalidShapeException {
    return read(new StringReader(value.toString().trim()));
  }

  @Override
  public Shape readIfSupported(Object value) throws InvalidShapeException {
    String v = value.toString().trim();
    char first = v.charAt(0);
    if(first >= '0' && first <= '9') {
      try {
        return read(new StringReader(v));
      } catch (ParseException e) {
      } catch (IOException e) {
      }
    }
    return null;
  }

  // --------------------------------------------------------------
  // Read GeoJSON
  // --------------------------------------------------------------

  @Override
  public final Shape read(Reader r) throws ParseException, IOException
  {
    XReader reader = new XReader(r);
    Double arg = null;
    
    Shape last = null;
    List<Shape> shapes = null;
    while(!reader.isDone()) {
      char event = reader.readKey();
      if(event<'0' || event > '9') {
        if(event == PolyshapeWriter.KEY_SEPERATOR) {
          continue; // read the next key
        }
        throw new ParseException("expecting a shape key.  not '"+event+"'", -1);
      }

      if(last!=null) {
        if(shapes==null) {
          shapes = new ArrayList<Shape>();
        }
        shapes.add(last);
      }
      arg = null;
      
      if(reader.peek()==PolyshapeWriter.KEY_ARG_START) {
        reader.readKey(); // skip the key
        arg = reader.readDouble();
        if(reader.readKey()!=PolyshapeWriter.KEY_ARG_END) {
          throw new ParseException("expecting an argument end", -1);
        }
      }
      if(reader.isEvent()) {
        throw new ParseException("Invalid input. Event should be followed by data", -1);
      }
      
      switch(event) {
        case PolyshapeWriter.KEY_POINT: {
          last = ctx.makePoint(reader.readLat(), reader.readLng());
          break;
        }
        case PolyshapeWriter.KEY_LINE: {
          if(arg!=null) {
            last = ctx.makeBufferedLineString(reader.readPoints(ctx), arg.doubleValue());
          }
          else {
            last = ctx.makeLineString(reader.readPoints(ctx));
          }
          break;
        }
        case PolyshapeWriter.KEY_BOX: {
          Point lowerLeft = ctx.makePoint(reader.readLat(), reader.readLng());
          Point upperRight = ctx.makePoint(reader.readLat(), reader.readLng());
          last = ctx.makeRectangle(lowerLeft, upperRight);
          break;
        }
        case PolyshapeWriter.KEY_MULTIPOINT : {
          List<Point> points = reader.readPoints(ctx);
          if(shapes==null) {
            shapes = new ArrayList<Shape>(points.size()+1);
          }
          shapes.addAll(points);
          break;
        }
        case PolyshapeWriter.KEY_CIRCLE : {
          if(arg==null) {
            throw new IllegalArgumentException("the input should have a radius argument");
          }
          last = ctx.makeCircle(reader.readLat(), reader.readLng(), arg.doubleValue());
          break;
        }
        case PolyshapeWriter.KEY_POLYGON: {
          last = readPolygon(reader);
          break;
        }
        default: {
          throw new ParseException("unhandled key: "+event, -1);
        }
      }
    }
    
    if(shapes!=null) {
      if(last!=null) {
        shapes.add(last);
      }
      return makeCollection(shapes);
    }
    return last;
  }
  
  protected Shape readPolygon(XReader reader) throws IOException {
    throw new IllegalArgumentException("This reader does not support polygons");
  }

  /**
   * from Apache 2.0 licensed:
   * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
   */
  public static class XReader {
    int lat = 0;
    int lng = 0;
    
    int head = -1;
    final Reader input;
    
    public XReader(final Reader input) throws IOException {
      this.input = input;
      head = input.read();
    }
    
    public List<Point> readPoints(SpatialContext ctx) throws IOException {
      List<Point> points = new ArrayList<Point>();
      while(isData()) {
        points.add(ctx.makePoint(readLat(), readLng()));
      }
      return points;
    }
    
    public List<double[]> readPoints() throws IOException {
      List<double[]> points = new ArrayList<double[]>();
      while(isData()) {
        points.add(new double[]{readLat(), readLng()});
      }
      return points;
    }
    
    public double readLat() throws IOException {
      lat += readInt();
      return lat * 1e-5;
    }

    public double readLng() throws IOException {
      lng += readInt();
      return lng * 1e-5;
    }
    
    public double readDouble() throws IOException {
      return readInt() * 1e-5;
    }
    
    public int peek() {
      return head;
    }

    public char readKey() throws IOException {
      lat = lng = 0; // reset the offset
      char key = (char)head;
      head = input.read();
      return key;
    }

    public boolean isData() {
      return head >= '?';
    }

    public boolean isDone() {
      return head < 0;
    }
    
    public boolean isEvent() {
      return head > 0 && head < '?';
    }
    
    int readInt() throws IOException
    {
      int b;
      int result = 1;
      int shift = 0;
      do {
        b = head - 63 - 1;
        result += b << shift;
        shift += 5;
        
        head = input.read();
      } while (b >= 0x1f);
      return (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
    }
  }
}