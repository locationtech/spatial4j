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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.impl.BufferedLine;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;


/**
 * Wrap the 'Encoded Polyline Algorithm Format' defined in: 
 * <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Google Maps API</a>
 * with flags to encode various Shapes:  Point, Line, Polygon, etc
 * 
 * For more details, see <a href="https://github.com/locationtech/spatial4j/blob/master/FORMATS.md#polyshape">FORMATS.md</a>
 * 
 * This format works well for geographic shapes (-180...+180 / -90...+90), but is not appropriate for other coordinate systems
 * 
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Google Maps API</a>
 * @see PolyshapeReader
 */
public class PolyshapeWriter implements ShapeWriter {

  public PolyshapeWriter(SpatialContext ctx, SpatialContextFactory factory) {

  }

  @Override
  public String getFormatName() {
    return ShapeIO.POLY;
  }


  @Override
  public void write(Writer output, Shape shape) throws IOException {
    if (shape == null) {
      throw new NullPointerException("Shape can not be null");
    }
    write(new Encoder(output), shape);
  }

  public void write(Encoder enc, Shape shape) throws IOException {
    if (shape instanceof Point) {
      Point v = (Point) shape;
      enc.write(KEY_POINT);
      enc.write(v.getX(), v.getY());
      return;
    }
    if (shape instanceof Rectangle) {
      Rectangle v = (Rectangle) shape;
      enc.write(KEY_BOX);
      enc.write(v.getMinX(), v.getMinY());
      enc.write(v.getMaxX(), v.getMaxY());
      return;
    }
    if (shape instanceof BufferedLine) {
      BufferedLine v = (BufferedLine) shape;
      enc.write(KEY_LINE);
      if(v.getBuf()>0) {
        enc.writeArg(v.getBuf());
      }
      enc.write(v.getA().getX(), v.getA().getY());
      enc.write(v.getB().getX(), v.getB().getY());
      return;
    }
    if (shape instanceof BufferedLineString) {
      BufferedLineString v = (BufferedLineString) shape;
      enc.write(KEY_LINE);
      if(v.getBuf()>0) {
        enc.writeArg(v.getBuf());
      }
      BufferedLine last = null;
      Iterator<BufferedLine> iter = v.getSegments().iterator();
      while (iter.hasNext()) {
        BufferedLine seg = iter.next();
        enc.write(seg.getA().getX(), seg.getA().getY());
        last = seg;
      }
      if (last != null) {
        enc.write(last.getB().getX(), last.getB().getY());
      }
      return;
    }
    if (shape instanceof Circle) {
      // See: https://github.com/geojson/geojson-spec/wiki/Proposal---Circles-and-Ellipses-Geoms
      Circle v = (Circle) shape;
      Point center = v.getCenter();
      double radius = v.getRadius();
      enc.write(KEY_CIRCLE);
      enc.writeArg(radius);
      enc.write(center.getX(), center.getY());
      return;
    }
    if (shape instanceof ShapeCollection) {
      ShapeCollection v = (ShapeCollection) shape;
      Iterator<Shape> iter = v.iterator();
      while(iter.hasNext()) {
        write(enc, iter.next());
        if(iter.hasNext()) {
          enc.seperator();
        }
      }
      return;
    }
    enc.writer.write("{unkwnwon " + LegacyShapeWriter.writeShape(shape) +"}");
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
  
  public static final char KEY_POINT      = '0';
  public static final char KEY_LINE       = '1';
  public static final char KEY_POLYGON    = '2';
  public static final char KEY_MULTIPOINT = '3';
  public static final char KEY_CIRCLE     = '4';
  public static final char KEY_BOX        = '5';
  
  public static final char KEY_ARG_START  = '(';
  public static final char KEY_ARG_END    =  ')';
  public static final char KEY_SEPERATOR  = ' ';
    

  /**
   * Encodes a sequence of LatLngs into an encoded path string.
   * 
   * from Apache 2.0 licensed:
   * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
   */
  public static class Encoder {
    long lastLat = 0;
    long lastLng = 0;
    
    final Writer writer;
    
    public Encoder(Writer writer) {
      this.writer = writer;
    }
    
    public void seperator() throws IOException {
      writer.write(KEY_SEPERATOR);
      lastLat = lastLng = 0;
    }

    public void startRing() throws IOException {
      writer.write(KEY_ARG_START);
      lastLat = lastLng = 0;
    }
    
    public void write(char event) throws IOException {
      writer.write(event);
      lastLat = lastLng = 0;
    }

    public void writeArg(double value) throws IOException {
      writer.write(KEY_ARG_START);
      encode(Math.round(value * 1e5));
      writer.write(KEY_ARG_END);
    }
    
    public void write(double latitude, double longitude) throws IOException {

      long lat = Math.round(latitude * 1e5);
      long lng = Math.round(longitude * 1e5);

      long dLat = lat - lastLat;
      long dLng = lng - lastLng;

      encode(dLat);
      encode(dLng);

      lastLat = lat;
      lastLng = lng;
    }
    
    private void encode(long v) throws IOException {
      v = v < 0 ? ~(v << 1) : v << 1;
      while (v >= 0x20) {
        writer.write(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
        v >>= 5;
      }
      writer.write(Character.toChars((int) (v + 63)));
    }
  }
}