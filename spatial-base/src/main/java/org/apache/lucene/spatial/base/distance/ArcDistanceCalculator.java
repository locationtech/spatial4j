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

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;

//TODO determine how this compares to HaversineDistance, and potentially delete if not useful
public class ArcDistanceCalculator extends AbstractDistanceCalculator {
  final DistanceUnits units;

  public ArcDistanceCalculator( DistanceUnits units ) {
    this.units = units;
  }

  @Override
  public double calculate(Point from, double toX, double toY) {
    return DistanceUtils.arcDistance(units,from.getX(),from.getY(),toX,toY);
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distance, SpatialContext ctx) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }
}
