/**
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

package org.apache.lucene.spatial.base.context.simple;

import org.apache.lucene.spatial.base.context.AbstractSpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Circle;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.HaversineWGS84Circle;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.spatial.base.shape.simple.RectangeImpl;

import java.text.NumberFormat;
import java.util.Locale;

public class SimpleSpatialContext extends AbstractSpatialContext {

  public SimpleSpatialContext(DistanceUnits units) {
    super(units);
  }

  public SimpleSpatialContext() {
    super( DistanceUnits.KILOMETERS );
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
  public Circle makeCircle(double x, double y, double distance) {
    return new HaversineWGS84Circle( makePoint( x, y ), distance, units.earthRadius(), this );
  }

  @Override
  public Rectangle makeRect(double minX, double maxX, double minY, double maxY) {
    return new RectangeImpl( minX, maxX, minY, maxY );
  }

  @Override
  public Point makePoint(double x, double y) {
    return new PointImpl(x,y);
  }
}
