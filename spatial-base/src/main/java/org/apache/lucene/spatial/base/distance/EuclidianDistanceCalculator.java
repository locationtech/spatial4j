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

package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

public class EuclidianDistanceCalculator implements DistanceCalculator {

  public double calculate(Point from, Shape shape) {
    if (Point.class.isInstance(shape)) {
      return calculate(from, (Point)shape);
    }
    throw new UnsupportedOperationException( "Distance to shape is not yet supported" );
  }

  public double calculate(Point from, Point point) {
    double result = 0;

    double v = from.getX() - point.getX();
    result += (v * v);

    v = from.getY() - point.getY();
    result += (v * v);

    return Math.sqrt(result);
  }
}
