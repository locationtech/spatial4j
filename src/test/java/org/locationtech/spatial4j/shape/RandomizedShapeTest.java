/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;
import static org.locationtech.spatial4j.shape.SpatialRelation.WITHIN;

/**
 * A base test class with utility methods to help test shapes.
 * Extends from RandomizedTest.
 */
public abstract class RandomizedShapeTest extends RandomizedTest {

  protected static final double EPS = 10e-9;

  protected SpatialContext ctx;//needs to be set ASAP

  /** Used to reduce the space of numbers to increase the likelihood that
   * random numbers become equivalent, and thus trigger different code paths.
   * Also makes some random shapes easier to manually examine.
   */
  protected final double DIVISIBLE = 2;// even coordinates; (not always used)

  protected RandomizedShapeTest() {
  }

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

    } else {
      if (maxX < minX) {
        double t = minX;
        minX = maxX;
        maxX = t;
      }
      minX = boundX(minX, ctx.getWorldBounds());
      maxX = boundX(maxX, ctx.getWorldBounds());
    }
    if (maxY < minY) {
      double t = minY;
      minY = maxY;
      maxY = t;
    }
    minY = boundY(minY, ctx.getWorldBounds());
    maxY = boundY(maxY, ctx.getWorldBounds());
    return ctx.makeRectangle(minX, maxX, minY, maxY);
  }

  public static double divisible(double v, double divisible) {
    return (int) (Math.round(v / divisible) * divisible);
  }

  protected double divisible(double v) {
    return divisible(v, DIVISIBLE);
  }

  /** reset()'s p, and confines to world bounds. Might not be divisible if
   * the world bound isn't divisible too.
   */
  protected Point divisible(Point p) {
    Rectangle bounds = ctx.getWorldBounds();
    double newX = boundX( divisible(p.getX()), bounds );
    double newY = boundY( divisible(p.getY()), bounds );
    p.reset(newX, newY);
    return p;
  }

  static double boundX(double i, Rectangle bounds) {
    return bound(i, bounds.getMinX(), bounds.getMaxX());
  }

  static double boundY(double i, Rectangle bounds) {
    return bound(i, bounds.getMinY(), bounds.getMaxY());
  }

  static double bound(double i, double min, double max) {
    if (i < min) return min;
    if (i > max) return max;
    return i;
  }

  protected void assertRelation(SpatialRelation expected, Shape a, Shape b) {
    assertRelation(null, expected, a, b);
  }

  protected void assertRelation(String msg, SpatialRelation expected, Shape a, Shape b) {
    _assertIntersect(msg, expected, a, b);
    //check flipped a & b w/ transpose(), while we're at it
    _assertIntersect(msg, expected.transpose(), b, a);
  }

  private void _assertIntersect(String msg, SpatialRelation expected, Shape a, Shape b) {
    SpatialRelation sect = a.relate(b);
    if (sect == expected)
      return;
    msg = ((msg == null) ? "" : msg+"\r") + a +" intersect "+b;
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
      assertEquals(msg,expected,sect);//always fails
    }
  }

  protected void assertEqualsRatio(String msg, double expected, double actual) {
    double delta = Math.abs(actual - expected);
    double base = Math.min(actual, expected);
    double deltaRatio = base==0 ? delta : Math.min(delta,delta / base);
    assertEquals(msg,0,deltaRatio, EPS);
  }

  protected int randomIntBetweenDivisible(int start, int end) {
    return randomIntBetweenDivisible(start, end, (int)DIVISIBLE);
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

  protected Rectangle randomRectangle(Point nearP) {
    Rectangle bounds = ctx.getWorldBounds();
    if (nearP == null)
      nearP = randomPointIn(bounds);

    double[] worldXRange = {bounds.getMinX(), bounds.getMaxX()};
    double[] worldYRange = {bounds.getMinY(), bounds.getMaxY()};
    double[] xRange = randomRange(rarely() ? 0 : nearP.getX(), worldXRange);
    double[] yRange = randomRange(rarely() ? 0 : nearP.getY(), worldYRange);

    return makeNormRect(
        divisible(xRange[0]),
        divisible(xRange[1]),
        divisible(yRange[0]),
        divisible(yRange[1]) );
  }

  private double[] randomRange(double near, double[] bounds) {
    final double boundsWidth = bounds[1] - bounds[0];
    double mid = near + randomGaussian() * boundsWidth / 6;
    double width = Math.abs(randomGaussian()) * boundsWidth / 6;//1/3rd
    return new double[]{mid - width / 2, mid + width / 2};
  }

  private double randomGaussianZeroTo(double max) {
    if (max == 0)
      return max;
    assert max > 0;
    double r;
    do {
      r = Math.abs(randomGaussian()) * (max * 0.50);
    } while (r > max);
    return r;
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

  protected Point randomPoint() {
    return randomPointIn(ctx.getWorldBounds());
  }

  protected Point randomPointIn(Circle c) {
    double d = c.getRadius() * randomDouble();
    double angleDEG = 360 * randomDouble();
    Point p = ctx.getDistCalc().pointOnBearing(c.getCenter(), d, angleDEG, ctx, null);
    assertEquals(CONTAINS,c.relate(p));
    return p;
  }

  protected Point randomPointIn(Rectangle r) {
    double x = r.getMinX() + randomDouble()*r.getWidth();
    double y = r.getMinY() + randomDouble()*r.getHeight();
    x = normX(x);
    y = normY(y);
    Point p = ctx.makePoint(x,y);
    assertEquals(CONTAINS,r.relate(p));
    return p;
  }

  protected Point randomPointIn(Shape shape) {
    if (!shape.hasArea())// or try the center?
      throw new UnsupportedOperationException("Need area to define shape!");
    Rectangle bbox = shape.getBoundingBox();
    Point p;
    do {
      p = randomPointIn(bbox);
    } while (!shape.relate(p).intersects());
    return p;
  }

  protected Point randomPointInOrNull(Shape shape) {
    if (!shape.hasArea())// or try the center?
      throw new UnsupportedOperationException("Need area to define shape!");
    Rectangle bbox = shape.getBoundingBox();
    for (int i = 0; i < 1000; i++) {
      Point p = randomPointIn(bbox);
      if (shape.relate(p).intersects()) {
        return p;
      }
    }
    return null;//tried too many times and failed
  }

  /** Tests that {@code left} >= {@code right}, but may be less if within some tolerance. */
  public static void assertGreaterOrEqual(double left, double right, double delta) {
    if (left > right) {
      return;
    }
    assertEquals(left, right, delta);
  }
}
