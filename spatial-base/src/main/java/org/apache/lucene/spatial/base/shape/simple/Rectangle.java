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

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * When minX > maxX, this will assume it is world coordinates that cross the
 * date line using degrees
 */
public class Rectangle implements BBox {

  private double minX;
  private double maxX;
  private double minY;
  private double maxY;

  public Rectangle(double minX, double maxX, double minY, double maxY) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  @Override
  public boolean hasArea() {
    return getWidth() > 0 && getHeight() > 0;
  }

  @Override
  public double getArea() {
    // CrossedDateline = true;
    if (minX > maxX) {
      return Math.abs(maxX + 360.0 - minX) * Math.abs(maxY - minY);
    }
    return Math.abs(maxX - minX) * Math.abs(maxY - minY);
  }

  @Override
  public boolean getCrossesDateLine() {
    return (minX > maxX);
  }

  @Override
  public double getHeight() {
    return maxY - minY;
  }

  @Override
  public double getWidth() {
    return maxX - minX;
  }

  @Override
  public double getMaxX() {
    return maxX;
  }

  @Override
  public double getMaxY() {
    return maxY;
  }

  @Override
  public double getMinX() {
    return minX;
  }

  @Override
  public double getMinY() {
    return minY;
  }

  @Override
  public boolean hasSize() {
    return maxX > minX && maxY > minY;
  }

  @Override
  public BBox getBoundingBox() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape shape, Object context) {
    if(!BBox.class.isInstance(shape)) {
      throw new IllegalArgumentException( "Rectangle can only be compared with another Extent" );
    }

    BBox ext = shape.getBoundingBox();
    if (ext.getMinX() > maxX ||
        ext.getMaxX() < minX ||
        ext.getMinY() > maxY ||
        ext.getMaxY() < minY) {
      return IntersectCase.OUTSIDE;
    }

    if (ext.getMinX() >= minX &&
        ext.getMaxX() <= maxX &&
        ext.getMinY() >= minY &&
        ext.getMaxY() <= maxY) {
      return IntersectCase.CONTAINS;
    }

    if (minX >= ext.getMinY() &&
        maxX <= ext.getMaxX() &&
        minY >= ext.getMinY() &&
        maxY <= ext.getMaxY()) {
      return IntersectCase.WITHIN;
    }
    return IntersectCase.INTERSECTS;
  }

  @Override
  public String toString() {
    return "[" + minX + "," + maxX + "," + minY + "," + maxY + "]";
  }
}
