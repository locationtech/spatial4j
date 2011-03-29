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

package org.apache.lucene.spatial.base.shape;

import java.io.IOException;

import org.apache.lucene.spatial.base.exception.InvalidShapeException;

public interface ShapeIO {

  /**
   * Read a shape from a given string (ie, X Y, XMin XMax... WKT)
   *
   * (1) Point: X Y
   *   1.23 4.56
   *
   * (2) BOX: XMin YMin XMax YMax
   *   1.23 4.56 7.87 4.56
   *
   * (3) PointDistance
   *   GeoCircle( x y distance=z )
   *   GeoCircle( x y d=z )
   *
   * (3) WKT
   *   POLYGON( ... )
   *   http://en.wikipedia.org/wiki/Well-known_text
   *
   */
  public Shape readShape(String value) throws InvalidShapeException;

  public String toString(Shape shape);

  public Shape readShape(byte[] bytes, int offset, int length) throws InvalidShapeException;

  public byte[] toBytes(Shape shape) throws IOException;

  public Point makePoint( double x, double y );

  public BBox makeBBox( double minX, double maxX, double minY, double maxY );
}
