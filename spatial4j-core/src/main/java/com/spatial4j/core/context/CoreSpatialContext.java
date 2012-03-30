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

import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUnits;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.GeoCircle;
import com.spatial4j.core.shape.ICircle;
import com.spatial4j.core.shape.IPoint;
import com.spatial4j.core.shape.IRectangle;
import com.spatial4j.core.shape.IShape;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

import java.text.NumberFormat;
import java.util.Locale;

public class CoreSpatialContext extends SpatialContext {

  public static CoreSpatialContext GEO_KM = new CoreSpatialContext(DistanceUnits.KILOMETERS);

  public CoreSpatialContext(DistanceUnits units) {
    this(units, null, null);
  }

  public CoreSpatialContext(DistanceUnits units, DistanceCalculator calculator, IRectangle worldBounds) {
    super(units, calculator, worldBounds);
  }

  @Override
  public IShape readShape(String value) throws InvalidShapeException {
    IShape s = super.readStandardShape( value );
    if( s == null ) {
      throw new InvalidShapeException( "Unable to read: "+value );
    }
    return s;
  }

  @Override
  public String toString(IShape shape) {
    if (IPoint.class.isInstance(shape)) {
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      IPoint point = (IPoint) shape;
      return nf.format(point.getX()) + " " + nf.format(point.getY());
    } else if (IRectangle.class.isInstance(shape)) {
      return writeRect((IRectangle) shape);
    }
    return shape.toString();
  }

  @Override
  public IPoint makePoint(double x, double y) {
    return new Point(normX(x),normY(y));
  }
}
