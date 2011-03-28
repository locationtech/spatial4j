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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;


public class Point2D implements Point {

  private double x;
  private double y;

  public Point2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public double getX() {
    return x;
  }

  @Override
  public double getY() {
    return y;
  }
  @Override
  public BBox getBoundingBox() {
    return new Rectangle(x, x, y, y);
  }

  @Override
  public IntersectCase intersect(Shape shape, Object context) {
    if(!BBox.class.isInstance(shape)) {
      throw new IllegalArgumentException("Point can only be compared with another Extent");
    }
    
    BBox ext = shape.getBoundingBox();
    if(x >= ext.getMinX() &&
       x <= ext.getMaxX() &&
       y >= ext.getMinY() &&
       y <= ext.getMaxY()){
      return IntersectCase.WITHIN;
    }
    return IntersectCase.OUTSIDE;
  }

  @Override
  public boolean hasArea() {
    return false;
  }
}
