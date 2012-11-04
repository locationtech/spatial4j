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

package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;

/**
 * A base test class with utility methods to help test shapes.
 * Extends from RandomizedTest.
 */
public abstract class RandomizedShapeTest extends RandomizedTest {

  protected static final double EPS = 10e-9;

  protected final SpatialContext ctx;

  public RandomizedShapeTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public static void checkShapesImplementEquals( Class[] classes ) {
    for( Class clazz : classes ) {
      try {
        clazz.getDeclaredMethod( "equals", Object.class );
      } catch (Exception e) {
        fail("Shape needs to define 'equals' : " + clazz.getName());
      }
      try {
        clazz.getDeclaredMethod( "hashCode" );
      } catch (Exception e) {
        fail("Shape needs to define 'hashCode' : " + clazz.getName());
      }
    }
  }

  //These few norm methods normalize the arguments for creating a shape to
  // account for the dateline. Some tests loop past the dateline or have offsets
  // that go past it and it's easier to have them coded that way and correct for
  // it here.  These norm methods should be used when needed, not frivolously.

  protected double normX(double x) {
    return ctx.isGeo() ? DistanceUtils.normLonDEG(x) : x;
  }

  protected double normY(double y) {
    return ctx.isGeo() ? DistanceUtils.normLatDEG(y) : y;
  }

  protected Rectangle makeNormRect(double minX, double maxX, double minY, double maxY) {
    if (ctx.isGeo()) {
      if (Math.abs(maxX - minX) >= 360) {
        minX = -180;
        maxX = 180;
      } else {
        minX = DistanceUtils.normLonDEG(minX);
        maxX = DistanceUtils.normLonDEG(maxX);
      }
    }
    return ctx.makeRectangle(minX, maxX, minY, maxY);
  }

  protected void assertRelation(String msg, SpatialRelation expected, Shape a, Shape b) {
    msg = a+" intersect "+b;//use different msg
    _assertIntersect(msg,expected,a,b);
    //check flipped a & b w/ transpose(), while we're at it
    _assertIntersect("(transposed) " + msg, expected.transpose(), b, a);
  }

  private void _assertIntersect(String msg, SpatialRelation expected, Shape a, Shape b) {
    SpatialRelation sect = a.relate(b);
    if (sect == expected)
      return;
    if (expected == WITHIN || expected == CONTAINS) {
      if (a.getClass().equals(b.getClass())) // they are the same shape type
        assertEquals(msg,a,b);
      else {
        //they are effectively points or lines that are the same location
        assertTrue(msg,!a.hasArea());
        assertTrue(msg,!b.hasArea());

        Rectangle aBBox = a.getBoundingBox();
        Rectangle bBBox = b.getBoundingBox();
        if (aBBox.getHeight() == 0 && bBBox.getHeight() == 0
            && (aBBox.getMaxY() == 90 && bBBox.getMaxY() == 90
          || aBBox.getMinY() == -90 && bBBox.getMinY() == -90))
          ;//== a point at the pole
        else
          assertEquals(msg, aBBox, bBBox);
      }
    } else {
      assertEquals(msg,expected,sect);
    }
  }

  protected void assertEqualsRatio(String msg, double expected, double actual) {
    double delta = Math.abs(actual - expected);
    double base = Math.min(actual, expected);
    double deltaRatio = base==0 ? delta : Math.min(delta,delta / base);
    assertEquals(msg,0,deltaRatio, EPS);
  }

  /** Returns a random integer between [start, end]. Integers between must be divisible by the 3rd argument. */
  protected int randomIntBetweenDivisible(int start, int end, int divisible) {
    // DWS: I tested this
    int divisStart = (int) Math.ceil( (start+1) / (double)divisible );
    int divisEnd = (int) Math.floor( (end-1) / (double)divisible );
    int divisRange = Math.max(0,divisEnd - divisStart + 1);
    int r = randomInt(1 + divisRange);//remember that '0' is counted
    if (r == 0)
      return start;
    if (r == 1)
      return end;
    return (r-2 + divisStart)*divisible;
  }

  protected Rectangle randomRectangle(int divisible) {
    double rX = randomIntBetweenDivisible(-180, 180, divisible);
    double rW = randomIntBetweenDivisible(0, 360, divisible);
    double rY1 = randomIntBetweenDivisible(-90, 90, divisible);
    double rY2 = randomIntBetweenDivisible(-90, 90, divisible);
    double rYmin = Math.min(rY1,rY2);
    double rYmax = Math.max(rY1,rY2);
    if (rW > 0 && rX == 180)
      rX = -180;
    return makeNormRect(rX, rX + rW, rYmin, rYmax);
  }

  protected Point randomPointWithin(Circle c) {
    double d = c.getRadius() * randomDouble();
    double angleDEG = 360 * randomDouble();
    Point p = ctx.getDistCalc().pointOnBearing(c.getCenter(), d, angleDEG, ctx, null);
    assertEquals(CONTAINS,c.relate(p));
    return p;
  }

  protected Point randomPointWithin(Rectangle r) {
    double x = r.getMinX() + randomDouble()*r.getWidth();
    double y = r.getMinY() + randomDouble()*r.getHeight();
    x = normX(x);
    y = normY(y);
    Point p = ctx.makePoint(x,y);
    assertEquals(CONTAINS,r.relate(p));
    return p;
  }
}
