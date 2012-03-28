/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.context;

import com.spatial4j.core.distance.*;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.simple.CircleImpl;
import com.spatial4j.core.shape.simple.GeoCircleImpl;
import com.spatial4j.core.shape.simple.RectangleImpl;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * This holds things like distance units, distance calculator, and world bounds.
 * Threadsafe & immutable.
 */
public abstract class SpatialContext {

  //These are non-null
  private final DistanceUnits units;
  private final DistanceCalculator calculator;
  private final Rectangle worldBounds;

  public static RectangleImpl GEO_WORLDBOUNDS = new RectangleImpl(-180,180,-90,90);
  public static RectangleImpl MAX_WORLDBOUNDS;
  static {
    double v = Double.MAX_VALUE;
    MAX_WORLDBOUNDS = new RectangleImpl(-v, v, -v, v);
  }
  
  protected final Double maxCircleDistance;//only for geo
  protected final boolean NUDGE = false;//TODO document

  /**
   *
   * @param units Required; and establishes geo vs cartesian.
   * @param calculator Optional; defaults to Haversine or cartesian depending on units.
   * @param worldBounds Optional; defaults to GEO_WORLDBOUNDS or MAX_WORLDBOUNDS depending on units.
   */
  protected SpatialContext(DistanceUnits units, DistanceCalculator calculator, Rectangle worldBounds) {
    if (units == null)
      throw new IllegalArgumentException("units can't be null");
    this.units = units;

    if (calculator == null) {
      calculator = isGeo()
          ? new GeodesicSphereDistCalc.Haversine(units.earthRadius())
          : new CartesianDistCalc();
    }
    this.calculator = calculator;

    if (worldBounds == null) {
      worldBounds = isGeo() ? GEO_WORLDBOUNDS : MAX_WORLDBOUNDS;
    } else {
      if (isGeo())
        assert new RectangleImpl(worldBounds).equals(GEO_WORLDBOUNDS);
      if (worldBounds.getCrossesDateLine())
        throw new IllegalArgumentException("worldBounds shouldn't cross dateline: "+worldBounds);
    }
    //copy so we can ensure we have the right implementation
    worldBounds = makeRect(worldBounds.getMinX(),worldBounds.getMaxX(),worldBounds.getMinY(),worldBounds.getMaxY());
    this.worldBounds = worldBounds;
    
    this.maxCircleDistance = isGeo() ? calculator.degreesToDistance(180) : null;
  }

  public DistanceUnits getUnits() {
    return units;
  }

  public DistanceCalculator getDistCalc() {
    return calculator;
  }

  public Rectangle getWorldBounds() {
    return worldBounds;
  }

  public double normX(double x) {
    if (isGeo()) {
      return DistanceUtils.normLonDEG(x);
    } else {
      return x;
    }
  }

  public double normY(double y) {
    if (isGeo()) {
      y = DistanceUtils.normLatDEG(y);
    }
    return y;
  }

  /**
   * Is this a geospatial context (true) or simply 2d spatial (false)
   */
  public boolean isGeo() {
    return getUnits().isGeo();
  }

  /**
   * Read a shape from a given string (ie, X Y, XMin XMax... WKT)
   *
   * (1) Point: X Y
   *   1.23 4.56
   *
   * (2) BOX: XMin YMin XMax YMax
   *   1.23 4.56 7.87 4.56
   *
   * (3) WKT
   *   POLYGON( ... )
   *   http://en.wikipedia.org/wiki/Well-known_text
   *
   */
  public abstract Shape readShape(String value) throws InvalidShapeException;

  public Point readLatCommaLonPoint(String value) throws InvalidShapeException {
    double[] latLon = ParseUtils.parseLatitudeLongitude(value);
    return makePoint(latLon[1],latLon[0]);
  }

  public abstract String toString(Shape shape);

  /** Construct a point. The parameters will be normalized. */
  public abstract Point makePoint( double x, double y );

  /** Construct a rectangle. The parameters will be normalized. */
  public Rectangle makeRect(double minX, double maxX, double minY, double maxY) {
    //--Normalize parameters
    if (isGeo()) {
      double delta = calcWidth(minX,maxX);
      if (delta >= 360) {
        //The only way to officially support complete longitude wrap-around is via western longitude = -180. We can't
        // support any point because 0 is undifferentiated in sign.
        minX = -180;
        maxX = 180;
      } else {
        minX = normX(minX);
        maxX = normX(maxX);
        assert Math.abs(delta - calcWidth(minX,maxX)) < 0.0001;//recompute delta; should be the same
      }
      if (minY > maxY) {
        throw new IllegalArgumentException("maxY must be >= minY");
      }
      if (minY < -90 || minY > 90 || maxY < -90 || maxY > 90)
        throw new IllegalArgumentException("minY or maxY is outside of -90 to 90 bounds. What did you mean?");
//        debatable what to do in this situation.
//        if (minY < -90) {
//          minX = -180;
//          maxX = 180;
//          maxY = Math.min(90,Math.max(maxY,-90 + (-90 - minY)));
//          minY = -90;
//        }
//        if (maxY > 90) {
//          minX = -180;
//          maxX = 180;
//          minY = Math.max(-90,Math.min(minY,90 - (maxY - 90)));
//          maxY = 90;
//        }

    } else {
      //these normalizations probably won't do anything since it's not geo but should probably call them any way.
      minX = normX(minX);
      maxX = normX(maxX);
      minY = normY(minY);
      maxY = normY(maxY);
    }
    return new RectangleImpl( minX, maxX, minY, maxY );
  }

  private double calcWidth(double minX,double maxX) {
    double w = maxX - minX;
    if (w < 0) {//only true when minX > maxX (WGS84 assumed)
      w += 360;
      assert w >= 0;
    }
    return w;
  }


  /** Construct a circle. The parameters will be normalized. */
  public Circle makeCircle(double x, double y, double distance) {
    return makeCircle(makePoint(x,y),distance);
  }

  /**
   * @param point
   * @param distance The units of "distance" should be the same as {@link #getUnits()}.
   */
  public Circle makeCircle(Point point, double distance) {
    if (distance < 0)
      throw new InvalidShapeException("distance must be >= 0; got "+distance);
    if (isGeo())
      return new GeoCircleImpl( point, Math.min(distance,maxCircleDistance), this );
    else
      return new CircleImpl( point, distance, this );
  }


  protected Shape readStandardShape(String str) {
    if (str.length() < 1) {
      throw new InvalidShapeException(str);
    }

    if(Character.isLetter(str.charAt(0))) {
      if( str.startsWith( "Circle(" ) ) {
        int idx = str.lastIndexOf( ')' );
        if( idx > 0 ) {
          String body = str.substring( "Circle(".length(), idx );
          StringTokenizer st = new StringTokenizer(body, " ");
          String token = st.nextToken();
          Point pt;
          if (token.indexOf(',') != -1) {
            pt = readLatCommaLonPoint(token);
          } else {
            double x = Double.parseDouble(token);
            double y = Double.parseDouble(st.nextToken());
            pt = makePoint(x,y);
          }
          Double d = null;

          String arg = st.nextToken();
          idx = arg.indexOf( '=' );
          if( idx > 0 ) {
            String k = arg.substring( 0,idx );
            if( k.equals( "d" ) || k.equals( "distance" ) ) {
              d = Double.parseDouble( arg.substring(idx+1));
            }
            else {
              throw new InvalidShapeException( "unknown arg: "+k+" :: " +str );
            }
          }
          else {
            d = Double.parseDouble(arg);
          }
          if( st.hasMoreTokens() ) {
            throw new InvalidShapeException( "Extra arguments: "+st.nextToken()+" :: " +str );
          }
          if( d == null ) {
            throw new InvalidShapeException( "Missing Distance: "+str );
          }
          //NOTE: we are assuming the units of 'd' is the same as that of the spatial context.
          return makeCircle(pt, d);
        }
      }
      return null;
    }

    if (str.indexOf(',') != -1)
      return readLatCommaLonPoint(str);
    StringTokenizer st = new StringTokenizer(str, " ");
    double p0 = Double.parseDouble(st.nextToken());
    double p1 = Double.parseDouble(st.nextToken());
    if (st.hasMoreTokens()) {
      double p2 = Double.parseDouble(st.nextToken());
      double p3 = Double.parseDouble(st.nextToken());
      if (st.hasMoreTokens())
        throw new InvalidShapeException("Only 4 numbers supported (rect) but found more: "+str);
      return makeRect(p0, p2, p1, p3);
    }
    return makePoint(p0, p1);
  }

  public String writeRect(Rectangle rect) {
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(6);
    nf.setMinimumFractionDigits(6);

    return
      nf.format(rect.getMinX()) + " " +
      nf.format(rect.getMinY()) + " " +
      nf.format(rect.getMaxX()) + " " +
      nf.format(rect.getMaxY());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"{" +
        "units=" + units +
        ", calculator=" + calculator +
        ", worldBounds=" + worldBounds +
        '}';
  }

}
