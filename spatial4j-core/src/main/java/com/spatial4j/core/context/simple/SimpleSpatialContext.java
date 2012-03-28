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

package com.spatial4j.core.context.simple;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUnits;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.simple.CircleImpl;
import com.spatial4j.core.shape.simple.GeoCircleImpl;
import com.spatial4j.core.shape.simple.PointImpl;
import com.spatial4j.core.shape.simple.RectangleImpl;

import java.text.NumberFormat;
import java.util.Locale;

public class SimpleSpatialContext extends SpatialContext {

  public static SimpleSpatialContext GEO_KM = new SimpleSpatialContext(DistanceUnits.KILOMETERS);

  public SimpleSpatialContext(DistanceUnits units) {
    this(units, null, null);
  }

  public SimpleSpatialContext(DistanceUnits units, DistanceCalculator calculator, Rectangle worldBounds) {
    super(units, calculator, worldBounds);
  }

  @Override
  public Shape readShape(String value) throws InvalidShapeException {
    Shape s = super.readStandardShape( value );
    if( s == null ) {
      throw new InvalidShapeException( "Unable to read: "+value );
    }
    return s;
  }

  @Override
  public String toString(Shape shape) {
    if (Point.class.isInstance(shape)) {
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      Point point = (Point) shape;
      return nf.format(point.getX()) + " " + nf.format(point.getY());
    } else if (Rectangle.class.isInstance(shape)) {
      return writeRect((Rectangle) shape);
    }
    return shape.toString();
  }

  @Override
  public Point makePoint(double x, double y) {
    return new PointImpl(normX(x),normY(y));
  }
}
