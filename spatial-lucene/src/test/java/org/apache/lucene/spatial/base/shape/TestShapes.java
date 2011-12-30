/*
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

package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.RandomSeed;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.apache.lucene.spatial.base.shape.IntersectCase.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author dsmiley
 */
public class TestShapes {

  private Random random;

  protected SpatialContext getGeoContext() {
    return new SimpleSpatialContext(DistanceUnits.KILOMETERS);
  }

  protected SpatialContext getNonGeoContext() {
    return new SimpleSpatialContext(DistanceUnits.EUCLIDEAN);
  }

  @Before
  public void beforeClass() {
    random = new Random(RandomSeed.seed());
  }

  @Test
  public void testSimplePoint() {
    SpatialContext ctx = getNonGeoContext();
    Point pt = ctx.makePoint(0,0);
    String msg = pt.toString();

    //test equals & hashcode
    Point pt2 = ctx.makePoint(0,0);
    assertEquals(msg, pt, pt2);
    assertEquals(msg, pt.hashCode(), pt2.hashCode());

    assertFalse(msg,pt.hasArea());
    assertEquals(msg,pt.getCenter(),pt);
    Rectangle bbox = pt.getBoundingBox();
    assertFalse(msg,bbox.hasArea());
    assertEquals(msg,pt,bbox.getCenter());

    assertIntersect(msg, CONTAINS, pt, pt2, ctx);
    assertIntersect(msg, OUTSIDE, pt, ctx.makePoint(0, 1), ctx);
    assertIntersect(msg, OUTSIDE, pt, ctx.makePoint(1, 0), ctx);
    assertIntersect(msg, OUTSIDE, pt, ctx.makePoint(1, 1), ctx);
  }

  @Test
  public void testGeoRectangle() {
    SpatialContext ctx = getGeoContext();
    double[] lons = new double[]{0,45,160,180,-45,-175, -180};//minX
    for (double lon : lons) {
      double[] lonWs = new double[]{0,20,180,200,355, 360};//width
      for (double lonW : lonWs) {
        testRectangle(lon, lonW, 0, 0, ctx);
        testRectangle(lon, lonW, -10, 10, ctx);
        testRectangle(lon, lonW, 80, 10, ctx);//polar cap
        testRectangle(lon, lonW, -90, 180, ctx);//full lat range
      }
    }

    //Test geo rectangle intersections
    testRectIntersect(ctx);
  }

  @Test
  public void testSimpleRectangle() {
    SpatialContext ctx = getNonGeoContext();
    double[] minXs = new double[]{-1000,-360,-180,-20,0,20,180,1000};
    for (double minX : minXs) {
      double[] widths = new double[]{0,10,180,360,400};
      for (double width : widths) {
        testRectangle(minX, width, 0, 0, ctx);
        testRectangle(minX, width, -10, 10, ctx);
        testRectangle(minX, width, 5, 10, ctx);
      }
    }

    testRectIntersect(ctx);
  }

  private void testRectangle(double minX, double width, double minY, double height, SpatialContext ctx) {
    Rectangle r = ctx.makeRect(minX, minX + width, minY, minY+height);
    //test equals & hashcode of duplicate
    Rectangle r2 = ctx.makeRect(minX, minX + width, minY, minY+height);
    assertEquals(r,r2);
    assertEquals(r.hashCode(),r2.hashCode());

    String msg = r.toString();

    assertEquals(msg, width != 0 && height != 0, r.hasArea());
    assertEquals(msg, width != 0 && height != 0, r.getArea() > 0);

    assertEqualsPct(msg, height, r.getHeight());
    assertEqualsPct(msg, width, r.getWidth());
    Point center = r.getCenter();
    msg += " ctr:"+center;
    //System.out.println(msg);
    assertIntersect(msg, CONTAINS, r, center, ctx);

    DistanceCalculator dc = ctx.getDistanceCalculator();
    double dUR = dc.calculate(center, r.getMaxX(), r.getMaxY());
    double dLR = dc.calculate(center, r.getMaxX(), r.getMinY());
    double dUL = dc.calculate(center, r.getMinX(), r.getMaxY());
    double dLL = dc.calculate(center, r.getMinX(), r.getMinY());

    assertEquals(msg,width != 0 || height != 0, dUR != 0);
    if (dUR != 0)
      assertTrue(dUR > 0 && dLL > 0);
    assertEqualsPct(msg, dUR, dUL);
    assertEqualsPct(msg, dLR, dLL);
    if (!ctx.isGeo() || center.getY() == 0)
      assertEqualsPct(msg, dUR, dLL);
  }

  // TODO Should this go into Rectangle or ctx API?
  private boolean touchesPole(SpatialContext ctx, Rectangle r) {
    if (!ctx.isGeo())
      return false;
    return r.getMaxY()==90 || r.getMaxY()==-90;
  }

  private void testRectIntersect(SpatialContext ctx) {
    final double INCR = 45;
    final double Y = 10;
    for(double left = -180; left <= 180; left += INCR) {
      for(double right = left; right - left <= 360; right += INCR) {
        Rectangle r = ctx.makeRect(left,right,-Y,Y);

        //test contains (which also tests within)
        for(double left2 = left; left2 <= right; left2 += INCR) {
          for(double right2 = left2; right2 <= right; right2 += INCR) {
            Rectangle r2 = ctx.makeRect(left2,right2,-Y,Y);
            assertIntersect(null, IntersectCase.CONTAINS, r, r2, ctx);
          }
        }
        //test point contains
        assertIntersect(null,IntersectCase.CONTAINS, r, ctx.makePoint(left,Y),ctx);

        //test outside
        for(double left2 = right+INCR; left2 - left < 360; left2 += INCR) {
          for(double right2 = left2; right2 - left < 360; right2 += INCR) {
            Rectangle r2 = ctx.makeRect(left2,right2,-Y,Y);
            assertIntersect(null, IntersectCase.OUTSIDE, r, r2, ctx);

            //test point outside
            assertIntersect(null,IntersectCase.OUTSIDE, r, ctx.makePoint(left2,Y),ctx);
          }
        }
        //test intersect
        for(double left2 = left+INCR; left2 <= right; left2 += INCR) {
          for(double right2 = right+INCR; right2 - left < 360; right2 += INCR) {
            Rectangle r2 = ctx.makeRect(left2,right2,-Y,Y);
            assertIntersect(null, IntersectCase.INTERSECTS, r, r2, ctx);
          }
        }

      }
    }
  }

  @Test
  public void testSimpleCircle() {
    SpatialContext ctx = getNonGeoContext();
    double[] theXs = new double[]{-10,0,10};
    for (double x : theXs) {
      double[] theYs = new double[]{-20,0,20};
      for (double y : theYs) {
        testCircle(x, y, 0, ctx);
        testCircle(x, y, 5, ctx);
      }
    }
    //INTERSECTION:
    //Start with some static tests that have shown to cause failures at some point:
    assertEquals("getX not getY",INTERSECTS,ctx.makeCircle(107,-81,147).intersect(ctx.makeRect(92,121,-89,74),ctx));

    testCircleIntersect(ctx);
  }

  @Test
  public void testGeoCircle() {
    SpatialContext ctx = getGeoContext();

    //--Start with some static tests that once failed:

    //Bug: numeric edge at pole, fails to init
    ctx.makeCircle(
        110,-12,ctx.getDistanceCalculator().degreesToDistance(90 + 12));
    
    {
      //Bug in which distance was being confused as being in the same coordinate system as x,y.
      double distDeltaToPole = 0.001;//1m
      double distDeltaToPoleDEG = ctx.getDistanceCalculator().distanceToDegrees(distDeltaToPole);
      double dist = 1;//1km
      double distDEG = ctx.getDistanceCalculator().distanceToDegrees(dist);
      Circle c = ctx.makeCircle(0,90-distDeltaToPoleDEG-distDEG,dist);
      Rectangle cBBox = c.getBoundingBox();
      Rectangle r = ctx.makeRect(cBBox.getMaxX()*0.99,cBBox.getMaxX()+1,c.getCenter().getY(),c.getCenter().getY());
      assertEquals(INTERSECTS,c.getBoundingBox().intersect(r, ctx));
      assertEquals("dist != xy space",INTERSECTS,c.intersect(r,ctx));//once failed here
    }

    assertEquals("wrong estimate",OUTSIDE,ctx.makeCircle(-166,59,5226.2).intersect(ctx.makeRect(36,66,23,23),ctx));

    assertEquals("bad CONTAINS (dateline)",INTERSECTS,ctx.makeCircle(56,-50,12231.5).intersect(ctx.makeRect(108,26,39,48),ctx));

    assertEquals("bad CONTAINS (backwrap2)",INTERSECTS,
        ctx.makeCircle(112,-3,10118.8/*91*/).intersect(ctx.makeRect(-163,29,-38,10),ctx));
    
    //--Now proceed with systematic testing:

    double distToOpposeSide = ctx.getUnits().earthRadius()*Math.PI;
    assertEquals(ctx.getWorldBounds(),ctx.makeCircle(0,0,distToOpposeSide).getBoundingBox());
    //assertEquals(ctx.makeCircle(0,0,distToOpposeSide/2 - 500).getBoundingBox());

    double[] theXs = new double[]{-180,-45,90};
    for (double x : theXs) {
      double[] theYs = new double[]{-90,-45,0,45,90};
      for (double y : theYs) {
        testCircle(x, y, 0, ctx);
        testCircle(x, y, 500, ctx);
        testCircle(x, y, ctx.getUnits().earthRadius()*6, ctx);
      }
    }

    testCircleIntersect(ctx);
  }

  private void testCircle(double x, double y, double dist, SpatialContext ctx) {
    Circle c = ctx.makeCircle(x, y, dist);
    String msg = c.toString();
    final Circle c2 = ctx.makeCircle(ctx.makePoint(x, y), dist);
    assertEquals(c, c2);
    assertEquals(c.hashCode(),c2.hashCode());

    assertEquals(msg,dist > 0, c.hasArea());
    final Rectangle bbox = c.getBoundingBox();
    assertEquals(msg,dist > 0, bbox.getArea() > 0);
    if (!ctx.isGeo()) {
      //if not geo then units of dist == units of x,y
      assertEqualsPct(msg, bbox.getHeight(), dist*2);
      assertEqualsPct(msg, bbox.getWidth(), dist*2);
    }
    assertIntersect(msg, CONTAINS, c , c.getCenter(), ctx);
    assertIntersect(msg, CONTAINS, bbox, c, ctx);
  }
  
  private void testCircleIntersect(SpatialContext ctx) {
    //Now do some randomized tests:
    int i_C = 0, i_I = 0, i_W = 0, i_O = 0;//counters for the different intersection cases
    int laps = 0;
    while(i_C < 10 || i_I < 10 || i_W < 10 || i_O < 10) {
      laps++;
      double cX = -180 + random.nextInt(360);
      double cY = -90 + random.nextInt(181);//includes +90
      double cR = random.nextInt(181);
      double cR_dist = ctx.getDistanceCalculator().calculate(ctx.makePoint(0,0),0,cR);
      Circle c = ctx.makeCircle(cX, cY, cR_dist);

      double rX = -180 + random.nextInt(360);
      double rW = random.nextInt(361);
      double rY1 = -90 + random.nextInt(181);
      double rY2 = -90 + random.nextInt(181);
      double rYmin = Math.min(rY1,rY2);
      double rYmax = Math.max(rY1,rY2);
      Rectangle r = ctx.makeRect(rX, rX+rW, rYmin, rYmax);

      IntersectCase ic = c.intersect(r, ctx);

      Point p;
      switch (ic) {
        case CONTAINS: 
          i_C++;
          p = randomPointWithin(random,r,ctx);
          assertEquals(CONTAINS,c.intersect(p,ctx));
          break;
        case INTERSECTS: 
          i_I++;
          //hard to test anything here; instead we'll test it separately
          break;
        case WITHIN:
          i_W++;
          p = randomPointWithin(random,c,ctx);
          assertEquals(CONTAINS,r.intersect(p,ctx));
          break;
        case OUTSIDE:
          i_O++;
          p = randomPointWithin(random,r,ctx);
          assertEquals(OUTSIDE,c.intersect(p,ctx));
          break;
        default: fail(""+ic);
      }
    }
    System.out.println("Laps: "+laps);

    //TODO deliberately test INTERSECTS based on known intersection point
  }

  private Point randomPointWithin(Random random, Circle c, SpatialContext ctx) {
    double d = c.getDistance() * random.nextDouble();
    double angleRAD = Math.toRadians(360*random.nextDouble());
    Point p = ctx.getDistanceCalculator().pointOnBearingRAD(c.getCenter(),d,angleRAD,ctx);
    assertEquals(CONTAINS,c.intersect(p,ctx));
    return p;
  }

  private Point randomPointWithin(Random random, Rectangle r, SpatialContext ctx) {
    double x = r.getMinX() + random.nextDouble()*r.getWidth();
    double y = r.getMinY() + random.nextDouble()*r.getHeight();
    Point p = ctx.makePoint(x,y);
    assertEquals(CONTAINS,r.intersect(p,ctx));
    return p;
  }

  //TODO test circle intersect, esp. world wrap, dateline wrap

  private void assertIntersect(String msg, IntersectCase expected, Shape a, Shape b, SpatialContext ctx ) {
    msg = a+" intersect "+b;//use different msg
    _assertIntersect(msg,expected,a,b,ctx);
    //check flipped a & b w/ transpose(), while we're at it
    _assertIntersect("(transposed) " + msg, expected.transpose(), b, a, ctx);
  }
  private void _assertIntersect(String msg, IntersectCase expected, Shape a, Shape b, SpatialContext ctx ) {
    IntersectCase sect = a.intersect(b, ctx);
    if (sect == expected)
      return;
    if (expected == WITHIN || expected == CONTAINS) {
      if (a.getClass().equals(b.getClass())) // they are the same shape type
        assertEquals(msg,a,b);
      else {
        //they are effectively points or lines that are the same location
        assertTrue(msg,!a.hasArea());
        assertTrue(msg,!b.hasArea());
        assertEquals(msg,a.getBoundingBox(),b.getBoundingBox());
      }
    } else {
      assertEquals(msg,expected,sect);
    }
  }

  void assertEqualsPct(String msg, double expected, double actual) {
    double delta = Math.abs(expected * 0.07);// TODO 7%!  I don't like that having it any smaller breaks. Why?
    //System.out.println(delta);
    assertEquals(msg,expected,actual, delta);
  }

}
