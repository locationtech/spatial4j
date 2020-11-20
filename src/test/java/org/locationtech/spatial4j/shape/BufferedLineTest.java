/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.locationtech.spatial4j.TestLog;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.impl.BufferedLine;
import org.locationtech.spatial4j.shape.impl.PointImpl;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BufferedLineTest extends RandomizedTest {

  private final SpatialContext ctx = new SpatialContextFactory()
    {{geo = false; worldBounds = new RectangleImpl(-100, 100, -50, 50, null);}}.newSpatialContext();

  @Rule
  public TestLog testLog = TestLog.instance;
//SpatialContext.GEO ;//

  public static void logShapes(final BufferedLine line, final Rectangle rect) {
    String lineWKT =
        "LINESTRING(" + line.getA().getX() + " " + line.getA().getY() + "," +
            line.getB().getX() + " " + line.getB().getY() + ")";
    System.out.println(
        "GEOMETRYCOLLECTION(" + lineWKT + "," + rectToWkt(line.getBoundingBox
            ()) + ")");

    String rectWKT = rectToWkt(rect);
    System.out.println(rectWKT);
  }

  static private String rectToWkt(Rectangle rect) {
    return "POLYGON((" + rect.getMinX() + " " + rect.getMinY() + "," +
        rect.getMaxX() + " " + rect.getMinY() + "," +
        rect.getMaxX() + " " + rect.getMaxY() + "," +
        rect.getMinX() + " " + rect.getMaxY() + "," +
        rect.getMinX() + " " + rect.getMinY() + "))";
  }

  @Test
  public void distance() {
    //negative slope
    testDistToPoint(ctx.makePoint(7, -4), ctx.makePoint(3, 2),
        ctx.makePoint(5, 6), 3.88290);
    //positive slope
    testDistToPoint(ctx.makePoint(3, 2), ctx.makePoint(7, 5),
        ctx.makePoint(5, 6), 2.0);
    //vertical line
    testDistToPoint(ctx.makePoint(3, 2), ctx.makePoint(3, 8),
        ctx.makePoint(4, 3), 1.0);
    //horiz line
    testDistToPoint(ctx.makePoint(3, 2), ctx.makePoint(6, 2),
        ctx.makePoint(4, 3), 1.0);
  }

  private void testDistToPoint(Point pA, Point pB, Point pC, double dist) {
    if (dist > 0) {
      assertFalse(new BufferedLine(pA, pB, dist * 0.999, ctx).contains(pC));
    } else {
      assert dist == 0;
      assertTrue(new BufferedLine(pA, pB, 0, ctx).contains(pC));
    }
    assertTrue(new BufferedLine(pA, pB, dist * 1.001, ctx).contains(pC));
  }

  @Test
  public void misc() {
    //pa == pb
    Point pt = ctx.makePoint(10, 1);
    BufferedLine line = new BufferedLine(pt, pt, 3, ctx);
    assertTrue(line.contains(ctx.makePoint(10, 1 + 3 - 0.1)));
    assertFalse(line.contains(ctx.makePoint(10, 1 + 3 + 0.1)));
  }

  @Test
  @Repeat(iterations = 15)
  public void quadrants() {
    //random line
    BufferedLine line = newRandomLine();
//    if (line.getA().equals(line.getB()))
//      return;//this test doesn't work
    Rectangle rect = newRandomLine().getBoundingBox();
    //logShapes(line, rect);
    //compute closest corner brute force
    ArrayList<Point> corners = quadrantCorners(rect);
    // a collection instead of 1 value due to ties
    Collection<Integer> farthestDistanceQuads = new LinkedList<>();
    double farthestDistance = -1;
    int quad = 1;
    for (Point corner : corners) {
      double d = line.getLinePrimary().distanceUnbuffered(corner);
      if (Math.abs(d - farthestDistance) < 0.000001) {//about equal
        farthestDistanceQuads.add(quad);
      } else if (d > farthestDistance) {
        farthestDistanceQuads.clear();
        farthestDistanceQuads.add(quad);
        farthestDistance = d;
      }
      quad++;
    }
    //compare results
    int calcClosestQuad = line.getLinePrimary().quadrant(rect.getCenter());
    assertTrue(farthestDistanceQuads.contains(calcClosestQuad));
  }

  private BufferedLine newRandomLine() {
    Point pA = new PointImpl(randomInt(9), randomInt(9), ctx);
    Point pB = new PointImpl(randomInt(9), randomInt(9), ctx);
    int buf = randomInt(5);
    return new BufferedLine(pA, pB, buf, ctx);
  }

  private ArrayList<Point> quadrantCorners(Rectangle rect) {
    ArrayList<Point> corners = new ArrayList<>(4);
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMinY()));
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMinY()));
    return corners;
  }

  @Test
  public void testRectIntersect() {
    new RectIntersectionTestHelper<BufferedLine>(ctx) {

      @Override
      protected BufferedLine generateRandomShape(Point nearP) {
        Rectangle nearR = randomRectangle(nearP);
        ArrayList<Point> corners = quadrantCorners(nearR);
        int r4 = randomInt(3);//0..3
        Point pA = corners.get(r4);
        Point pB = corners.get((r4 + 2) % 4);
        double maxBuf = Math.max(nearR.getWidth(), nearR.getHeight());
        double buf = Math.abs(randomGaussian()) * maxBuf / 4;
        buf = randomInt((int) divisible(buf));
        return new BufferedLine(pA, pB, buf, ctx);
      }

      protected Point randomPointInEmptyShape(BufferedLine shape) {
        int r = randomInt(1);
        if (r == 0) return shape.getA();
        //if (r == 1)
        return shape.getB();
//        Point c = shape.getCenter();
//        if (shape.contains(c));
      }
    }.testRelateWithRectangle();
  }

  private BufferedLine newBufLine(int x1, int y1, int x2, int y2, int buf) {
    Point pA = ctx.makePoint(x1, y1);
    Point pB = ctx.makePoint(x2, y2);
    if (randomBoolean()) {
      return new BufferedLine(pB, pA, buf, ctx);
    } else {
      return new BufferedLine(pA, pB, buf, ctx);
    }
  }

}