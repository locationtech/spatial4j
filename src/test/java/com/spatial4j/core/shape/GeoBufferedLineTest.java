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
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.spatial4j.core.TestLog;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.impl.BufferedLine;
import com.spatial4j.core.shape.impl.GeoBufferedLine;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class GeoBufferedLineTest extends RandomizedTest {

  private final SpatialContext ctx = new SpatialContextFactory()
    {{geo = true;}}.newSpatialContext();

  @Rule
  public TestLog testLog = TestLog.instance;
//SpatialContext.GEO ;//

 /* public static void logShapes(final GeoBufferedLine line, final Rectangle rect) {
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
*/
  @Test
  public void distance() {
    System.out.println("Tested GeoBufferedLine");
    //negative slope
    testDistToPoint(ctx.makePoint(0, 0), ctx.makePoint(5, 0),
            ctx.makePoint(0, 90), 90);
    testDistToPoint(ctx.makePoint(0, 0), ctx.makePoint(5, 0),
            ctx.makePoint(0, 45), 45);
    testDistToPoint(ctx.makePoint(0, 0), ctx.makePoint(0,5),
            ctx.makePoint(5, 0), 5);
    testDistToPoint(ctx.makePoint(0, 0), ctx.makePoint(0,5),
            ctx.makePoint(90, 0), 90);
    testDistToPoint(ctx.makePoint(10, 0), ctx.makePoint(10,5),
            ctx.makePoint(30, 0), 20);

  }

  private void testLessToPoint(Point pA, Point pB, Point pC, double dist) {
    assertTrue(new GeoBufferedLine(pA, pB, dist * 1.001, ctx).contains(pC));
  }

  private void testDistToPoint(Point pA, Point pB, Point pC, double dist) {

    if (dist > 0) {
      assertFalse(new GeoBufferedLine(pA, pB, dist * 0.999, ctx).contains(pC));
    } else {
      assert dist == 0;
      assertTrue(new GeoBufferedLine(pA, pB, 0, ctx).contains(pC));
    }
    assertTrue(new GeoBufferedLine(pA, pB, dist * 1.001, ctx).contains(pC));

    flipPoint(pA);
    flipPoint(pB);
    flipPoint(pC);

    if (dist > 0) {
      assertFalse(new GeoBufferedLine(pA, pB, dist * 0.999, ctx).contains(pC));
    } else {
      assert dist == 0;
      assertTrue(new GeoBufferedLine(pA, pB, 0, ctx).contains(pC));
    }
    assertTrue(new GeoBufferedLine(pA, pB, dist * 1.001, ctx).contains(pC));

  }

  private void flipPoint(Point p) {
    p.reset(-1*p.getX(),-1*p.getY());
  }
/*
  @Test
  public void misc() {
    //pa == pb
    Point pt = ctx.makePoint(10, 1);
    GeoBufferedLine line = new GeoBufferedLine(pt, pt, 3, ctx);
    assertTrue(line.contains(ctx.makePoint(10, 1 + 3 - 0.1)));
    assertFalse(line.contains(ctx.makePoint(10, 1 + 3 + 0.1)));
  }
  @Test
  @Repeat(iterations = 15)
  public void quadrants() {
    //random line
    GeoBufferedLine line = newRandomLine();
//    if (line.getA().equals(line.getB()))
//      return;//this test doesn't work
    Rectangle rect = newRandomLine().getBoundingBox();
    //logShapes(line, rect);
    //compute closest corner brute force
    ArrayList<Point> corners = quadrantCorners(rect);
    // a collection instead of 1 value due to ties
    Collection<Integer> farthestDistanceQuads = new LinkedList<Integer>();
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
  private GeoBufferedLine newRandomLine() {
    Point pA = new PointImpl(randomInt(90), randomInt(90), ctx);
    Point pB = new PointImpl(randomInt(90), randomInt(90), ctx);
    int buf = randomInt(5);
    return new GeoBufferedLine(pA, pB, buf, ctx);
  }

  private ArrayList<Point> quadrantCorners(Rectangle rect) {
    ArrayList<Point> corners = new ArrayList<Point>(4);
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMinY()));
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMinY()));
    return corners;
  }

  @Test
  public void testRectIntersect() {
    new RectIntersectionTestHelper<GeoBufferedLine>(ctx) {

      @Override
      protected GeoBufferedLine generateRandomShape(Point nearP) {
        Rectangle nearR = randomRectangle(nearP);
        ArrayList<Point> corners = quadrantCorners(nearR);
        int r4 = randomInt(3);//0..3
        Point pA = corners.get(r4);
        Point pB = corners.get((r4 + 2) % 4);
        double maxBuf = Math.max(nearR.getWidth(), nearR.getHeight());
        double buf = Math.abs(randomGaussian());// * maxBuf / 4;
        buf = randomInt((int) divisible(buf));
        return new GeoBufferedLine(pA, pB, buf, ctx);
      }

      protected Point randomPointInEmptyShape(GeoBufferedLine shape) {
        int r = randomInt(1);
        if (r == 0) return shape.getA();
        //if (r == 1)
        return shape.getB();
//        Point c = shape.getCenter();
//        if (shape.contains(c));
      }
    }.testRelateWithRectangle();
  }

  private GeoBufferedLine newBufLine(int x1, int y1, int x2, int y2, int buf) {
    Point pA = ctx.makePoint(x1, y1);
    Point pB = ctx.makePoint(x2, y2);
    if (randomBoolean()) {
      return new GeoBufferedLine(pB, pA, buf, ctx);
    } else {
      return new GeoBufferedLine(pA, pB, buf, ctx);
    }
  }*/

}