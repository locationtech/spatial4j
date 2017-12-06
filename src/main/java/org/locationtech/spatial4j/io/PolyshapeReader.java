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


package org.locationtech.spatial4j.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.jts.geom.LinearRing;


/**
 * @see PolyshapeWriter
 */
public class PolyshapeReader implements ShapeReader {
  final SpatialContext ctx;
  final ShapeFactory shpFactory;

  public PolyshapeReader(SpatialContext ctx, SpatialContextFactory factory) {
    this.ctx = ctx;
    this.shpFactory = ctx.getShapeFactory();
  }

  @Override
  public String getFormatName() {
    return ShapeIO.POLY;
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
    XReader reader = new XReader(r, shpFactory);
    Double arg = null;
    
    Shape lastShape = null;
    List<Shape> shapes = null;
    while(!reader.isDone()) {
      char event = reader.readKey();
      if(event<'0' || event > '9') {
        if(event == PolyshapeWriter.KEY_SEPERATOR) {
          continue; // read the next key
        }
        throw new ParseException("expecting a shape key.  not '"+event+"'", -1);
      }

      if(lastShape!=null) {
        if(shapes==null) {
          shapes = new ArrayList<Shape>();
        }
        shapes.add(lastShape);
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
          lastShape = shpFactory.pointXY(shpFactory.normX(reader.readLat()), shpFactory.normY(reader.readLng()));
          break;
        }
        case PolyshapeWriter.KEY_LINE: {
          ShapeFactory.LineStringBuilder lineBuilder = shpFactory.lineString();
          reader.readPoints(lineBuilder);
          
          if(arg!=null) {
            lineBuilder.buffer(shpFactory.normDist(arg));
          }
          lastShape = lineBuilder.build();
          break;
        }
        case PolyshapeWriter.KEY_BOX: {
          double lat1 = shpFactory.normX(reader.readLat());
          double lon1 = shpFactory.normY(reader.readLng());
          lastShape = shpFactory.rect(lat1, shpFactory.normX(reader.readLat()), 
                  lon1, shpFactory.normY(reader.readLng()));
          break;
        }
        case PolyshapeWriter.KEY_MULTIPOINT : {
          lastShape = reader.readPoints(shpFactory.multiPoint()).build();
          break;
        }
        case PolyshapeWriter.KEY_CIRCLE : {
          if(arg==null) {
            throw new IllegalArgumentException("the input should have a radius argument");
          }
          lastShape = shpFactory.circle(shpFactory.normX(reader.readLat()), shpFactory.normY(reader.readLng()), 
                shpFactory.normDist(arg.doubleValue()));
          break;
        }
        case PolyshapeWriter.KEY_POLYGON: {
          lastShape = readPolygon(reader);
          break;
        }
        default: {
          throw new ParseException("unhandled key: "+event, -1);
        }
      }
    }
    
    if(shapes!=null) {
      if(lastShape!=null) {
        shapes.add(lastShape);
      }

      ShapeFactory.MultiShapeBuilder<Shape> multiBuilder = shpFactory.multiShape(Shape.class); 
      for (Shape shp : shapes) {
        multiBuilder.add(shp);
      }

      return multiBuilder.build();
    }
    return lastShape;
  }
  
  protected Shape readPolygon(XReader reader) throws IOException {
    ShapeFactory.PolygonBuilder polyBuilder = shpFactory.polygon();
    
    reader.readPoints(polyBuilder);

    if(!reader.isDone() && reader.peek()==PolyshapeWriter.KEY_ARG_START) {
      List<LinearRing> list = new ArrayList<LinearRing>();
      while(reader.isEvent() && reader.peek()==PolyshapeWriter.KEY_ARG_START) {
        reader.readKey(); // eat the event;
        reader.readPoints(polyBuilder.hole()).endHole();
      }
    }

    return polyBuilder.build();
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
    final ShapeFactory shpFactory;

    public XReader(final Reader input, ShapeFactory shpFactory) throws IOException {
      this.input = input;
      this.shpFactory = shpFactory;
      head = input.read();
    }
    
    public <T extends ShapeFactory.PointsBuilder> T readPoints(T builder) throws IOException {
      while(isData()) {
        builder.pointXY(shpFactory.normX(readLat()), shpFactory.normY(readLng()));
      }
      return builder;
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