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

package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Circle;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * The spatial context.  This holds things like:
 *  - spatial reference
 *  - units
 *  - ect
 */
public abstract class SpatialContext {

  private final Rectangle worldBoundsWGS84 = makeRect(-180, 180, -90, 90);

  public abstract DistanceUnits getUnits();

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
    double[] latLon = DistanceUtils.parseLatitudeLongitude(value);
    return makePoint(latLon[1],latLon[0]);
  }

  public abstract String toString(Shape shape);

  public abstract Point makePoint( double x, double y );

  public abstract Rectangle makeRect(double minX, double maxX, double minY, double maxY);


  public Circle makeCircle(double x, double y, double distance) {
    return makeCircle(makePoint(x,y),distance);
  }

  /**
   *
   * @param ctr
   * @param distance The units of "distance" should be the same as {@link #getUnits()}.
   * @return
   */
  public abstract Circle makeCircle(Point ctr, double distance);

  /**
   * Get a calculator that will work in this context
   */
  public DistanceCalculator getDistanceCalculator() {
    return getDistanceCalculator( null );
  }

  public abstract DistanceCalculator getDistanceCalculator( Class<? extends DistanceCalculator> clazz );

  /**
   * Returns the x,y bounds of the "world". By default this returns WGS84 -180,180,-90,90.
   */
  public Rectangle getWorldBounds() {
    return worldBoundsWGS84;
  }

}
