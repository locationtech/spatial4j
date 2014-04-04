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
import com.spatial4j.core.shape.impl.GeoBufferedLine;
import com.spatial4j.core.shape.impl.GreatCircle;
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Rule;
import org.junit.Test;
import sun.print.PSPrinterJob;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class GeoBufferedLineTest extends RandomizedTest {

  private final SpatialContext ctx = new SpatialContextFactory()
    {{geo = true;}}.newSpatialContext();

  @Rule
  //public String TestLog testLog = TestLog.instance;
//SpatialContext.GEO ;//



 public static String logShapes(final GeoBufferedLine line, final Rectangle rect, int num, Point[] points) {
    String lineWKT =
        "LINESTRING(" + line.getA().getX() + " " + line.getA().getY() + "," +
            line.getB().getX() + " " + line.getB().getY() + ")";

    boolean leftDraw = false;

    if((rect.getMaxX() - rect.getMinX()) > 180 ) {
      leftDraw = true;
    }


    String boxString  = "";
    if(leftDraw) {
      // debug
      //System.out.println("LEFT " + num);
      boxString =  rect.getMaxX() +"," + rect.getMaxY() +",0 " + (rect.getMinX() + 360) +"," + rect.getMaxY() +",0 " + (rect.getMinX() + 360) +"," + rect.getMinY() +",0 " + rect.getMaxX() +"," + rect.getMinY() +",0 " + rect.getMaxX() +"," + rect.getMaxY() +",0 \n";

      // boxString =  rect.getMaxX() +"," + rect.getMaxY() +",0 " + (rect.getMinX() + 360) +"," + rect.getMaxY() +",0 "  + rect.getMinX() +"," + rect.getMinY() +",0 " + rect.getMaxX() +"," + rect.getMinY() +",0 " + rect.getMaxX() +"," + rect.getMaxY() +",0 \n";
    } else {
      boxString = rect.getMinX() +"," + rect.getMaxY() +",0 " + rect.getMaxX() +"," + rect.getMaxY() +",0 " + rect.getMaxX() +"," + rect.getMinY() +",0 " + rect.getMinX() +"," + rect.getMinY() +",0 " + rect.getMinX() +"," + rect.getMaxY() +",0 \n";
    }


      String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
    "<Document>\n"+
    "<name>KmlFile "+ num +"</name> \n"+
    "<Style id=\"transPurpleLineGreenPoly\">\n"+
    "<LineStyle> \n"+
    "<color>7fff00ff</color>\n" +
    "<width>" + 1 + line.getBuffer() * 5 + "</width>\n"+
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
    "<coordinates> \n"+ boxString +
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
    "</Placemark> \n";

    if(points != null) {
      int i = 0;
      for(Point p: points) {
        kml += "<Placemark><Point> \n" +
        "<coordinates>"+ p.getX()+","+p.getY() +"</coordinates>\n" +
        "</Point></Placemark>";
      }
    }

    kml += "</Document> \n"+
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
    testDistToPoint(p(180,89),p(0,89),p(0,0),89);
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


  private double [][] coordinates = {{-99.50495175269295,-27.847143714750892,94.92744976763885,58.97453564417145},
          {-113.55873663082807,-37.38700137891473 ,108.9768420505157,56.82595028841023},
          {-114.13045387706195,-57.759601585537034,
                  66.42246194712695,7.61423855201417},{135.673532878296,19.749231296312367,
          99.89993687971368,54.595746732751735},{-113.55873663082807,-37.38700137891473,
          108.9768420505157,56.82595028841023},{-99.50495175269295,-27.847143714750892,
          94.92744976763885,58.97453564417145},{-137.4473549035918,-59.92702226097382,
          37.65126452060316,46.121912417763326},{-34.1896496909006,-4.55377853792414,
          145.29499975518303,42.11651349705701},{-132.7599266353268,-55.81481332371045,
          -92.47845748579186,-53.72410194167834},{-80.37659959947197,-59.29005255570004,
          121.42340168404372,48.274996665589015},{134.50864258825123,4.025328678586604,
          -79.42487585076405,-55.09328902724629},{-114.56774869654645,-46.80318469884033,
          -15.145331712139603,-40.95521632556603},{-106.80494427030459,-51.35562641913801,
          80.5612819857112,29.766393899451643},{135.673532878296,19.749231296312367,
          99.89993687971368,54.595746732751735},{2.1833,41.3833,-73.9400,40.67},
          {-128.27072030063476,-27.70757917535692,28.498386417001726,47.68996542540272},
          {-132.7599266353268,-55.81481332371045,-92.47845748579186,-53.72410194167834},{0,0,10,0}};


  private void writeVisualTestFile(String fileName, String contents) {
    Writer writer = null;
    try {
      writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream("test_visual/test_visual_" + fileName + ".kml"), "utf-8"));
      writer.write(contents);
    } catch (IOException ex) {
      // report
    } finally {
      try {
        writer.close();
      } catch (Exception ex) {
      }
  }
  }

  @Test
  public void testVisualShapeEquator() {

    // TODO: Buffer forces coverage of more than half of earth, opposite bounding box is drawn instead.
    // Do we handle this case?
    double[] test = coordinates[17];
    GeoBufferedLine equator = new GeoBufferedLine(ctx.makePoint(test[0], test[1]), ctx.makePoint(test[2], test[3]), 80, ctx);
    Rectangle r = equator.getBoundingBox();
    String s = logShapes(equator, equator.getBoundingBox(), 0,null);
    writeVisualTestFile("equator",s);
    }


  @Test
  public void testVisualShapeStatic() {
    int i = 0;

    GeoBufferedLine line;// = newRandomLine();

    for (double[] c : coordinates) {
      line = new GeoBufferedLine(ctx.makePoint(c[0], c[1]), ctx.makePoint(c[2], c[3]), 0, ctx);
      i++;
      String s = logShapes(line, line.getBoundingBox(), i,null);
      writeVisualTestFile("static_" + i + "",s);
    }
  }

  @Test
  public void testVisualShapeRandom() throws Exception {
    for (int i = 0; i < 10; i++) {

      GeoBufferedLine line = newRandomLine();
      String s = logShapes(line, line.getBoundingBox(), i,null);
      writeVisualTestFile(""+i+"",s);
    }
  }


  private GeoBufferedLine newRandomLine() {

    boolean randomA = randomBoolean();
    boolean randomB = randomBoolean();
    double random90A = randomDouble() * 90;
    double random180B = randomDouble() * 180;

    double rand90 =  randomDouble() * 60;
    double rand180 =  randomDouble() * 150;

    random90A = randomA ? rand90 : -1*rand90;
    random180B = randomA ? rand180 : -1*rand180;

    Point pA = ctx.makePoint(random180B,random90A);


    randomA = randomBoolean();
    randomB = randomBoolean();

    rand90 =  randomDouble() * 60;
    rand180 =  randomDouble() * 150;


    double random90AB = randomA ? rand90 : -1*rand90;
    double random180BB = randomA ? rand180 : -1*rand180;


    Point pB = ctx.makePoint(random180BB,random90AB);

    int buf = randomInt(3);
    return new GeoBufferedLine(pA, pB, buf, ctx);
  }

  @Test
  public void testBoundingBox() throws Exception {

    for(int i = 0; i < 10; i ++) {
      GeoBufferedLine line = newRandomLine();
      Rectangle bbox = line.getBoundingBox();
      Point[] points = randomPointsInBoundingBox(bbox);
      for(Point p: points) {
        assertEquals(SpatialRelation.CONTAINS , bbox.relate(p));
      }

      String s = logShapes(line, bbox, i, points);
      writeVisualTestFile("points_" + i,s);
    }

  }

  private Point[] randomPointsInBoundingBox(Rectangle box) {
    // 999 random points in box.
    Point[] points = new Point[1000];
    for(int i = 0; i < 1000; i ++) {
      double randomX = box.getMinX() + (box.getMaxX() - box.getMinX()) * randomDouble();
      double randomY = box.getMinY() + (box.getMaxY() - box.getMinY()) * randomDouble();
      points[i] = ctx.makePoint(randomX,randomY);
    }

    return points;
  }

  @Test
  public void testMoment() throws Exception {
    GeoBufferedLine line = new GeoBufferedLine(p(0,0),p(90,45),10,ctx);
    Rectangle rect = ctx.makeRectangle(90,100,44,60);
    System.out.println("Relate massive rect " + line.relate(rect));
  }

  private ArrayList<Point> quadrantCorners(Rectangle rect) {
    ArrayList<Point> corners = new ArrayList<Point>(4);
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMaxY()));
    corners.add(ctx.makePoint(rect.getMinX(), rect.getMinY()));
    corners.add(ctx.makePoint(rect.getMaxX(), rect.getMinY()));
    return corners;
  }



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
        double buf = Math.abs(randomGaussian()) * maxBuf / 4;
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