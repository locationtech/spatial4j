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

package org.apache.lucene.spatial.base.simple;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Radius;
import org.apache.lucene.spatial.base.Shape;


public class Radius2D implements Radius
{
  private Point2D point;
  private double radius;

  public Radius2D(Point2D p, double r)
  {
    this.point = p;
    this.radius = r;
  }

  //-----------------------------------------------------
  //-----------------------------------------------------

  @Override
  public Point2D getPoint() {
    return point;
  }

  @Override
  public double getRadius() {
    return radius;
  }

  @Override
  public BBox getBoundingBox() {
    return new Rectangle(
        point.getX()-radius, point.getX()+radius,
        point.getY()-radius, point.getY()+radius );
  }

  @Override
  public boolean hasArea() {
    return radius > 0;
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    // TODO... something better!
    return getBoundingBox().intersect(other, context);
  }
}
