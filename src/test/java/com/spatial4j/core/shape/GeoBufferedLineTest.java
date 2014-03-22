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
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Rule;
import org.junit.Test;
import sun.print.PSPrinterJob;

import java.io.*;
import java.util.Date;

public class GeoBufferedLineTest extends RandomizedTest {

  private final SpatialContext ctx = new SpatialContextFactory()
    {{geo = true;}}.newSpatialContext();

  @Rule
  //public String TestLog testLog = TestLog.instance;
//SpatialContext.GEO ;//



 public static String logShapes(final GeoBufferedLine line, final Rectangle rect) {
    String lineWKT =
        "LINESTRING(" + line.getA().getX() + " " + line.getA().getY() + "," +
            line.getB().getX() + " " + line.getB().getY() + ")";

    String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
    "<Document>\n"+
    "<name>KmlFile</name> \n"+
    "<Style id=\"transPurpleLineGreenPoly\">\n"+
    "<LineStyle> \n"+
    "<color>7fff00ff</color>\n" +
    "<width>1</width>\n"+
    "</LineStyle>\n"+
            "</Style> \n"+
            "<Style id=\"transBluePoly\"> \n"+
    "<LineStyle> \n"+
    "<width>1.5</width> \n"+
    "</LineStyle> \n"+
    "<PolyStyle> \n"+
    "<color>7dff0000</color> \n"+
    "</PolyStyle> \n"+
    "</Style> \n"+
    "<Placemark> \n"+
    "<name>Absolute</name> \n"+
    "<visibility>1</visibility> \n"+
    "<styleUrl>#transBluePoly</styleUrl> \n"+
    "<Polygon> \n"+
    "<tessellate>1</tessellate> \n"+
    "<outerBoundaryIs> \n"+
    "<LinearRing> \n"+
    "<coordinates> \n"+ rect.getMinX() +"," + rect.getMaxY() +",0 " + rect.getMaxX() +"," + rect.getMaxY() +",0 " + rect.getMaxX() +"," + rect.getMinY() +",0 " + rect.getMinX() +"," + rect.getMinY() +",0 " + rect.getMinX() +"," + rect.getMaxY() +",0 \n" +
    "        </coordinates> \n"+
    "</LinearRing> \n"+
    "</outerBoundaryIs> \n"+
    "</Polygon> \n"+
    "</Placemark> \n"+
           "<Placemark>\n"+
    "<name>Absolute</name>\n"+
    "<description>Transparent purple line</description> \n"+
    "<styleUrl>#transPurpleLineGreenPoly</styleUrl> \n"+
    "<LineString> \n"+
    "<tessellate>1</tessellate> \n"+
    "<coordinates> \n"+
            + line.getA().getX() + "," + line.getA().getY() + ",0\n" + line.getB().getX() + "," + line.getB().getY() + ",0\n" +
    "       </coordinates> \n"+
    "</LineString> \n"+
    "</Placemark> \n"+
    "</Document> \n"+
    "</kml>";

    return kml;

  }

  static private String rectToWkt(Rectangle rect) {
    return "POLYGON((" + rect.getMinX() + " " + rect.getMinY() + "," +
        rect.getMaxX() + " " + rect.getMinY() + "," +
        rect.getMaxX() + " " + rect.getMaxY() + "," +
        rect.getMinX() + " " + rect.getMaxY() + "," +
        rect.getMinX() + " " + rect.getMinY() + "))";
  }

  @Test
  public void testPerpendicular() throws Exception {
    Point a = p(-1,0);
    Point b = p(1,0);
    GeoBufferedLine line = new GeoBufferedLine(a, b, 0, ctx);
    GreatCircle l = line.getLinePerpendicular();
    assertFalse(line.getPerpA3d().getX() == 1 && line.getPerpA3d().getY() == 0 && line.getPerpA3d().getZ() == 0);
    assertFalse(line.getPerpB3d().getX() == 1 && line.getPerpB3d().getY() == 0);

    for (int i = 0; i < 20; i++) {
      double random90 = randomDouble() * 90;
      line = new GeoBufferedLine(p(-1 * random90, 0), p(random90, 0), 0, ctx);
      assertFalse(line.getPerpA3d().getX() == 1 && line.getPerpA3d().getY() == 0 && line.getPerpA3d().getZ() == 0);
      assertFalse(line.getPerpB3d().getX() == 1 && line.getPerpB3d().getY() == 0);
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

  @Test
  public void testVisualShape() throws Exception {
    GeoBufferedLine line = newRandomLine();
//    line = new GeoBufferedLine(ctx.makePoint(2.1833,41.3833),ctx.makePoint(-73.9400,40.67),0,ctx);
//    System.out.println("Angle " + line.getLinePrimary().getAngleDEG());
//    System.out.println("Highest Point: " + line.getLinePrimary().highestPoint(ctx));

    String s = logShapes(line, line.getBoundingBox());
    Writer writer = null;

    try {
      writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream("text.kml"), "utf-8"));
      writer.write(s);
    } catch (IOException ex) {
      // report
    } finally {
      try {writer.close();} catch (Exception ex) {}
    }

  }


  private GeoBufferedLine newRandomLine() {

    boolean randomA = randomBoolean();
    boolean randomB = randomBoolean();
    double random90A = randomDouble() * 90;
    double random180B = randomDouble() * 180;

    double rand90 =  randomDouble() * 90;
    double rand180 =  randomDouble() * 180;

    random90A = randomA ? rand90 : -1*rand90;
    random180B = randomA ? rand180 : -1*rand180;

    Point pA = ctx.makePoint(random180B,random90A);


    randomA = randomBoolean();
    randomB = randomBoolean();

    rand90 =  randomDouble() * 90;
    rand180 =  randomDouble() * 180;


    double random90AB = randomA ? rand90 : -1*rand90;
    double random180BB = randomA ? rand180 : -1*rand180;


    Point pB = ctx.makePoint(random180BB,random90AB);

    int buf = randomInt(5);
    return new GeoBufferedLine(pA, pB, buf, ctx);
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