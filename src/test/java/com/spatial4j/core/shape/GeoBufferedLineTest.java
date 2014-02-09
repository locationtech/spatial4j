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
import com.spatial4j.core.TestLog;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.shape.impl.GeoBufferedLine;
import com.spatial4j.core.shape.impl.GreatCircle;
import org.junit.Rule;
import org.junit.Test;
import sun.print.PSPrinterJob;

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
  public void testPerpendicular() throws Exception {
    GeoBufferedLine line = new GeoBufferedLine(p(-1,0), p(1,0), 0, ctx);
    GreatCircle l = line.getLinePerpendicular();
    assertFalse(l.getA().getX() == 1 && l.getA().getY() == 0 && l.getA().getZ() == 0);
    assertFalse(l.getB().getX() == 1 && l.getB().getY() == 0);

    for (int i = 0; i < 20; i++) {
      double random90 = randomDouble() * 90;
      line = new GeoBufferedLine(p(-1 * random90, 0), p(random90, 0), 0, ctx);
      assertFalse(l.getA().getX() == 1 && l.getA().getY() == 0 && l.getA().getZ() == 0);
      assertFalse(l.getB().getX() == 1 && l.getB().getY() == 0);
      l = line.getLinePerpendicular();
      for (int j = 0; j <= 90; j++) {
        assertEquals(l.distanceToPoint(p(0, j)), 0, 0.000001);
        assertEquals(l.distanceToPoint(p(0, -1 * j)), 0, 0.000001);
      }
    }
  }

  @Test
  public void distance() {
    System.out.println("Tested GeoBufferedLine");
    //negative slope
    testDistToPoint(p(0, 0), p(5, 0),p(0, 90), 90);
    testDistToPoint(p(0, 0), p(5, 0),p(0, 45), 45);
    testDistToPoint(p(0, 0), p(0, 5),p(5, 0), 5);
    testDistToPoint(p(0, 0), p(0, 5),p(90, 0), 90);
    testDistToPoint(p(10, 0), p(10, 5),p(30, 0), 20);
    testDistToPoint(p(10, 0), p(10, 5),p(30, 0), 20);

    for (int i = 0; i < 20; i++) {
      double random90 = randomDouble() * 90;
      double random90Two = randomDouble() * 90;
      testDistToPoint(p(90, 0), p(0, 0),p(0, random90), random90);
      testDistToPoint(p(0, 90), p(0, 0),p(random90Two, 0), random90Two);
      testDistToPoint(p(0, 0), p(90, 45),p(90, random90Two), Math.abs(45-random90Two));

    }
    testDistToPoint(p(-90, 45), p(90, 45),p(90, 45),0);
    testDistToPoint(p(-90, -45), p(0, 0),p(-90, 0),45);
    testDistToPoint(p(90, 89), p(0, 0),p(0,90),1);

  }

  private Point p(double x, double y) {
    return ctx.makePoint(x, y);
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
    Point pt = p(10, 1);
    GeoBufferedLine line = new GeoBufferedLine(pt, pt, 3, ctx);
    assertTrue(line.contains(p(10, 1 + 3 - 0.1)));
    assertFalse(line.contains(p(10, 1 + 3 + 0.1)));
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
    corners.add(p(rect.getMaxX(), rect.getMaxY()));
    corners.add(p(rect.getMinX(), rect.getMaxY()));
    corners.add(p(rect.getMinX(), rect.getMinY()));
    corners.add(p(rect.getMaxX(), rect.getMinY()));
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
    Point pA = p(x1, y1);
    Point pB = p(x2, y2);
    if (randomBoolean()) {
      return new GeoBufferedLine(pB, pA, buf, ctx);
    } else {
      return new GeoBufferedLine(pA, pB, buf, ctx);
    }
  }*/

}