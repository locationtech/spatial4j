/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;


public class GeoBufferedLine implements Shape {

  private final Point a;
  private final Point b;
  private final double buffer;
  private final double bufferPerp;
  private final SpatialContext ctx;

  @Override
  public Rectangle getBoundingBox() {

    double minX, maxX;
    double minY, maxY;

    if(a.getX() > a.getY()) {
      maxX = a.getX();
      minX = b.getX();
    } else {
      maxX = b.getX();
      minX = a.getX();
    }

    if(a.getY() > b.getY()) {
      maxY = a.getY();
      minY = b.getY();
    } else {
      maxY = b.getY();
      minY = a.getY();
    }

    Point highestPoint = linePrimary.highestPoint(ctx);
    Point lowestPoint = linePrimary.lowestPoint(ctx);

    

    return null;

  }

  @Override
  public boolean hasArea() {
    return false;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return 0;
  }

  @Override
  public Point getCenter() {
    return null;
  }

  @Override
  public Shape getBuffered(SpatialContext ctx, double distance) {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  private final GreatCircle linePrimary;
  private final GreatCircle linePerpendicular;

  public GeoBufferedLine(Point a, Point b, double buffer, SpatialContext ctx) {
    this.a = a;
    this.b = b;
    this.buffer = buffer;
    this.bufferPerp = bufferPerpendicular(a, b);
    this.ctx = ctx;

    linePrimary = new GreatCircle(a,b);

    Point3d midPoint = Point3d.midPoint(linePrimary.getA(),linePrimary.getB());

    Point3d a3d = linePrimary.getA();
    Point3d b3d = linePrimary.getB();

    Point3d midAVector = new Point3d(a3d.getX()-midPoint.getX(),a3d.getY()-midPoint.getY(),a3d.getZ()-midPoint.getZ());
    Point3d midBVector = new Point3d(0-midPoint.getX(),0-midPoint.getY(),0-midPoint.getZ());

    // midPoint a3d X midPoint b3d
    // Normal Vector to the plane, at point midPoint
    Point3d normal = Point3d.crossProductPoint(midAVector,midBVector);

    // Calculate a second point
    Point3d secondaryPoint = new Point3d(midPoint.getX() + normal.getX()*2,midPoint.getY() + normal.getY()*2,midPoint.getZ() + normal.getZ()*2);

    linePerpendicular = new GreatCircle(midPoint,secondaryPoint);
  }

  private double bufferPerpendicular(Point a, Point b) {
    double xA,xB,yA,yB;
    xA = DistanceUtils.toRadians(a.getX());
    yA = DistanceUtils.toRadians(a.getY());
    xB = DistanceUtils.toRadians(b.getX());
    yB = DistanceUtils.toRadians(b.getY());
    return DistanceUtils.toDegrees(DistanceUtils.distHaversineRAD(xA,yA,xB,yB));
  }

  public double getBuffer() {
    return this.buffer;
  }

  @Override
  public SpatialRelation relate(Shape other) {
    return null;
  }

  /* public for testing */
  public GreatCircle getLinePrimary() {
    return linePrimary;
  }

  /* public for testing */
  public GreatCircle getLinePerpendicular() {
    return linePerpendicular;
  }

  public boolean contains(Point p) {
    // TODO: Extend past end of line
    if(linePrimary.distanceToPoint(p) <= buffer && linePerpendicular.distanceToPoint(p) <= bufferPerp) {
      return true;
    } else {
      return false;
    }
  }



}
