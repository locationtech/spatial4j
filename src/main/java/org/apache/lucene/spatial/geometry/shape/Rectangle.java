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

package org.apache.lucene.spatial.geometry.shape;


/**
 * Rectangle shape.
 *
 * <p><font color="red"><b>NOTE:</b> This API is still in
 * flux and might change in incompatible ways in the next
 * release.</font>
 */
public class Rectangle implements Geometry2D {
  private final Point2D ptMin, ptMax;

  public Rectangle() {
    ptMin=new Point2D(-1, 1);
    ptMax=new Point2D(1, 1);
  }

  public Rectangle(Point2D ptMin, Point2D ptMax) {
    assert ptMin.getX() <= ptMax.getX() && ptMin.getY() <= ptMax.getY();
    this.ptMin=new Point2D(ptMin);
    this.ptMax=new Point2D(ptMax);
  }

  public Rectangle(double x1, double y1, double x2, double y2) {
    this(new Point2D(x1,y1),new Point2D(x2,y2));
  }

  @Override
  public String toString() {
    return "[" + ptMin + "," + ptMax + "]";
  }

  public double area() {
    return (ptMax.getX() - ptMin.getX()) * (ptMax.getY() - ptMin.getY());
  }

  public double getWidth()  { return ptMax.getX() - ptMin.getX(); }
  public double getHeight() { return ptMax.getY() - ptMin.getY(); }

  public Point2D centroid() {
    return new Point2D( (ptMin.getX() + ptMax.getX()) / 2,
                  (ptMin.getY() + ptMax.getY()) / 2);
  }

  @Override
  public Rectangle boundingRectangle() {
    return this;
  }

  public boolean contains(Point2D p) {
    return p.getX() >= ptMin.getX() &&
      p.getX() <= ptMax.getX() &&
      p.getY() >= ptMin.getY() &&
      p.getY() <= ptMax.getY();
  }

  public void translate(Vector2D v) {
    ptMin.add(v);
    ptMax.add(v);
  }

  public Point2D getMaxPoint() {
    return ptMax;
  }

  public Point2D getMinPoint() {
    return ptMin;
  }

  public Point2D getMinXMaxYPoint() {
    return new Point2D(getMinX(), getMaxY());
  }

  public Point2D getMaxXMinYPoint() {
    return new Point2D(getMaxX(), getMinY());
  }

  public double getMinX() {
    return ptMin.getX();
  }

  public double getMinY() {
    return ptMin.getY();
  }

  public double getMaxX() {
    return ptMax.getX();
  }

  public double getMaxY() {
    return ptMax.getY();
  }

  public IntersectCase intersect(Rectangle r) {
    if (getMaxY() >= r.getMaxY() && getMinY() <= r.getMinY() && getMaxX() >= r.getMaxX() && getMinX() <= r.getMinX())
      return IntersectCase.CONTAINS;
    if (  (getMinY() > r.getMaxY() || getMaxY() < r.getMinY()) ||
          (getMinX() > r.getMaxX() || getMaxX() < r.getMinX())
            )
      return IntersectCase.OUTSIDE;
    return IntersectCase.INTERSECTS;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ptMax == null) ? 0 : ptMax.hashCode());
    result = prime * result + ((ptMin == null) ? 0 : ptMin.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Rectangle other = (Rectangle) obj;
    if (ptMax == null) {
      if (other.ptMax != null)
        return false;
    } else if (!ptMax.equals(other.ptMax))
      return false;
    if (ptMin == null) {
      if (other.ptMin != null)
        return false;
    } else if (!ptMin.equals(other.ptMin))
      return false;
    return true;
  }

}
