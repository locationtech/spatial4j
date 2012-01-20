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

package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.RandomSeed;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class TestDistances {

  private final Random random = new Random(RandomSeed.seed());
  //NOTE!  These are sometimes modified by tests.
  private SpatialContext ctx;
  private double EPS;

  @Before
  public void beforeTest() {
    ctx = new SimpleSpatialContext(DistanceUnits.KILOMETERS);
    EPS = 10e-4;//delta when doing double assertions. Geo eps is not that small.
  }

  private DistanceCalculator dc() {
    return ctx.getDistCalc();
  }
  
  @Test
  public void testSomeDistances() {
    //See to verify: from http://www.movable-type.co.uk/scripts/latlong.html
    Point ctr = pLL(0,100);
    assertEquals(11100, dc().distance(ctr, pLL(10, 0)),3);
    assertEquals(11100, dc().distance(ctr, pLL(10, -160)),3);

    assertEquals(314.40338, dc().distance(pLL(1, 2), pLL(3, 4)),EPS);
  }

  @Test @Ignore("Temporary!  TODO")
  public void testHaversineBBox() {
    //first test known bug
    {
      double d = 6894.1;
      Point pCtr = pLL(-20, 84);
      Point pTgt = pLL(-42, 15);
      assertTrue(dc().distance(pCtr, pTgt) < d);
      //since the pairwaise distance is less than d, a bounding box from ctr with d should contain pTgt.
      Rectangle r = dc().calcBoxByDistFromPt(pCtr, d, ctx);
      assertEquals(IntersectCase.CONTAINS,r.intersect(pTgt,ctx));//once failed
    }

    for (int T = 0; T < 1000; T++) {
      int latSpan = random.nextInt(179);
      int remainderLat = 179 - latSpan;
      double lat = -90 + 0.5 + random.nextInt(remainderLat) + latSpan/2;
      double lon = -180 + random.nextDouble()*360;
      Point ctr = ctx.makePoint(lon,lat);
      double dist = dc().distance(ctr, lon, lat + latSpan / 2);

      String msg = "ctr: "+ctr+" dist: "+dist+" T:"+T;

      Rectangle r = dc().calcBoxByDistFromPt(ctr, dist, ctx);

      assertEquals(msg, dist, dc().distance(ctr, r.getMinX(), ctr.getY()), EPS);
    }

  }

  @Test
  public void testDistCalcPointOnBearing_cartesian() {
    ctx = new SimpleSpatialContext(DistanceUnits.CARTESIAN);
    EPS = 10e-6;//tighter epsilon (aka delta)
    for(int i = 0; i < 1000; i++) {
      testDistCalcPointOnBearing(random.nextInt(100));
    }
  }

  @Test
  public void testDistCalcPointOnBearing_geo() {
    //test known high delta
//    {
//      Point c = ctx.makePoint(-103,-79);
//      double angRAD = Math.toRadians(236);
//      double dist = 20025;
//      Point p2 = dc().pointOnBearingRAD(c, dist, angRAD, ctx);
//      //Pt(x=76.61200011750923,y=79.04946929870962)
//      double calcDist = dc().distance(c, p2);
//      assertEqualsRatio(dist, calcDist);
//    }
    double maxDist = ctx.getUnits().earthCircumference() / 2;
    for(int i = 0; i < 1000; i++) {
      int dist = random.nextInt((int) maxDist);
      EPS = (dist < maxDist*0.75 ? 10e-6 : 10e-3);
      testDistCalcPointOnBearing(dist);
    }
  }

  private void testDistCalcPointOnBearing(double dist) {
    for(int angDEG = 0; angDEG < 360; angDEG += random.nextInt(20)+1) {
      Point c = ctx.makePoint(random.nextInt(360),-90+random.nextInt(181));
      double angRAD = Math.toRadians(angDEG);

      //0 distance means same point
      Point p2 = dc().pointOnBearingRAD(c, 0, angRAD, ctx);
      assertEquals(c,p2);

      p2 = dc().pointOnBearingRAD(c, dist, angRAD, ctx);
      double calcDist = dc().distance(c, p2);
      assertEqualsRatio(dist, calcDist);
    }
  }

  private void assertEqualsRatio(double expected, double actual) {
    double delta = Math.abs(actual - expected);
    double base = Math.min(actual, expected);
    double deltaRatio = base==0 ? delta : Math.min(delta,delta / base);
    assertEquals(0,deltaRatio, EPS);
  }

  @Test
  public void testNormLat() {
    double[][] lats = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-90,-90},{90,90},{0,0}, {-100,-80},
        {-90-180,90},{-90-360,-90},{90+180,-90},{90+360,90},
        {-12+180,12}};
    for (double[] pair : lats) {
      assertEquals("input "+pair[0],pair[1],ctx.normY(pair[0]),0);
    }
    Random random = new Random(RandomSeed.seed());
    for(int i = -1000; i < 1000; i += random.nextInt(10)*10) {
      double d = ctx.normY(i);
      assertTrue(i + " " + d, d >= -90 && d <= 90);
    }
  }

  @Test
  public void testNormLon() {
    double[][] lons = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-180,-180},{180,-180},{0,0}, {-190,170},
        {-180-360,-180},{-180-720,-180},{180+360,-180},{180+720,-180}};
    for (double[] pair : lons) {
      assertEquals("input "+pair[0],pair[1],ctx.normX(pair[0]),0);
    }
    Random random = new Random(RandomSeed.seed());
    for(int i = -1000; i < 1000; i += random.nextInt(10)*10) {
      double d = ctx.normX(i);
      assertTrue(i + " " + d, d >= -180 && d < 180);
    }
  }

  @Test
  public void testDistToRadians() {
    assertDistToRadians(0);
    assertDistToRadians(500);
    assertDistToRadians(ctx.getUnits().earthRadius());
  }
  private void assertDistToRadians(double dist) {
    assertEquals(
        DistanceUtils.pointOnBearingRAD(0, 0, dist, DistanceUtils.DEG_90_AS_RADS, null, ctx.getUnits().earthRadius())[1],
        DistanceUtils.dist2Radians(dist,ctx.getUnits().earthRadius()),10e-5);
  }

//
//  private void assertPointEquals(Point expected, Point actual) {
//    final double delta = 0.0001;
//    assertEquals(expected.getX(), actual.getX(), delta);
//    assertEquals(expected.getY(), actual.getY(), delta);
//  }

  private boolean spans(Rectangle r) {
    return r.getWidth() == 360;
  }

  private Point pLL(double lat, double lon) {
    return ctx.makePoint(lon,lat);
  }

}
