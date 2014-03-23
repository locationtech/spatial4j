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
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

import java.util.ArrayList;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;


public class GeoBufferedLine implements Shape {

  private final Point a;
  private final Point b;

  private final double buffer;

  public Point getA() {
    return a;
  }

  public Point getB() {
    return b;
  }

  private final double bufferPerp;

  private final SpatialContext ctx;

  private final GreatCircle linePrimary;
  private final GreatCircle linePerpendicular;

  private final Point3d primeA3d;
  private final Point3d primeB3d;

  private final Point3d perpA3d;
  private final Point3d perpB3d;



  //TODO: Can a and b be the same point?
  public GeoBufferedLine(Point a, Point b, double buffer, SpatialContext ctx) {
    this.a = a;
    this.b = b;
    this.buffer = buffer;
    this.bufferPerp = bufferPerpendicular(a, b);
    this.ctx = ctx;

    Point3d[] points = GreatCircle.get3dPointsForGreatCircle(a,b);
    primeA3d = points[0];
    primeB3d = points[1];

    linePrimary = new GreatCircle(primeA3d,primeB3d);

    perpA3d = Point3d.midPoint(primeA3d,primeB3d);

    Point3d midAVector = new Point3d(primeA3d.getX()-perpA3d.getX(),primeA3d.getY()-perpA3d.getY(),primeA3d.getZ()-perpA3d.getZ());
    Point3d midBVector = new Point3d(0-perpA3d.getX(),0-perpA3d.getY(),0-perpA3d.getZ());

    // perpA3d a3d X perpA3d b3d
    // Normal Vector to the plane, at point perpA3d
    Point3d normal = Point3d.crossProductPoint(midAVector,midBVector);

    // Calculate a second point
    perpB3d = new Point3d(perpA3d.getX() + normal.getX()*2,perpA3d.getY() + normal.getY()*2,perpA3d.getZ() + normal.getZ()*2);



    linePerpendicular = new GreatCircle(perpA3d,perpB3d);
  }

  public Point3d getPrimeA3d() {
    return primeA3d;
  }

  public Point3d getPrimeB3d() {
    return primeB3d;
  }

  public Point3d getPerpA3d() {
    return perpA3d;
  }

  public Point3d getPerpB3d() {
    return perpB3d;
  }

  // does not include the bounding box.

  //TODO: Dateline / Extra Wrapping eg over the pole
  @Override
  public Rectangle getBoundingBox() {

    double minX, maxX;
    double minY, maxY;

    if(a.getX() > b.getX()) {
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

    // TODO: find which direction is the shortest, between the two points.
    // Find the Change in x, and y. Try both, see which is shorter.
    // Ie, calc delta x, add to one point, subtract x from the same point, see which one,
    // gives us the same


    boolean posXDirection = ((maxX - minX) > 180) ? true : false;
    //boolean posYDirection = ((maxY - minY) == 0 ) ? true : false;

    double xMax360,xMin360,xHighest360, xLowest360;

    xMax360 = makeAxis360(maxX);
    xMin360 = makeAxis360(minX);
    xHighest360 = makeAxis360(highestPoint.getX());
    xLowest360 = makeAxis360(lowestPoint.getX());

    // Moving from minx to maxx (Positive long)
    if(posXDirection) {
      if((maxY > 0) && (xMin360 >= xHighest360) && (xHighest360 >= xMax360)) {
        maxY = highestPoint.getY();
      }
      if(((minY < 0) && (xMin360 >= xLowest360) && (xLowest360) >= xMax360)) {
        minY = lowestPoint.getY();
      }
    } else {
      if((maxY > 0) && (minX <= highestPoint.getX()) && (highestPoint.getX() <= maxX)) {
        maxY = highestPoint.getY();
      }
      if(((minY < 0) && (minX <= lowestPoint.getX()) && (lowestPoint.getX() <= maxX))) {
        minY = lowestPoint.getY();
      }
    }

    if(posXDirection) {
      return ctx.makeRectangle(minX + buffer,maxX - buffer,minY - buffer,maxY + buffer);
    } else {
    return ctx.makeRectangle(minX - buffer,maxX + buffer,minY - buffer,maxY + buffer);
    }
  }


  private double makeAxis360(double x) {
    if(x < 0) {
      return 360 + x;
    } else {
      return x;
    }

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
    throw new UnsupportedOperationException();
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
