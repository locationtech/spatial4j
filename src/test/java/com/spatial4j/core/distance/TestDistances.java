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

package com.spatial4j.core.distance;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Before;
import org.junit.Test;

import static com.spatial4j.core.distance.DistanceUtils.DEG_TO_KM;
import static com.spatial4j.core.distance.DistanceUtils.KM_TO_DEG;

public class TestDistances extends RandomizedTest {

  //NOTE!  These are sometimes modified by tests.
  private SpatialContext ctx;
  private double EPS;

  @Before
  public void beforeTest() {
    ctx = SpatialContext.GEO;
    EPS = 10e-4;//delta when doing double assertions. Geo eps is not that small.
  }

  private DistanceCalculator dc() {
    return ctx.getDistCalc();
  }

  @Test
  public void testSomeDistances() {
    //See to verify: from http://www.movable-type.co.uk/scripts/latlong.html
    Point ctr = pLL(0,100);
    assertEquals(11100, dc().distance(ctr, pLL(10, 0)) * DEG_TO_KM, 3);
    double deg = dc().distance(ctr, pLL(10, -160));
    assertEquals(11100, deg * DEG_TO_KM, 3);

    assertEquals(314.40338, dc().distance(pLL(1, 2), pLL(3, 4)) * DEG_TO_KM, EPS);
  }

  @Test
  public void testCalcBoxByDistFromPt() {
    //first test regression
    {
      double d = 6894.1 * KM_TO_DEG;
      Point pCtr = pLL(-20, 84);
      Point pTgt = pLL(-42, 15);
      assertTrue(dc().distance(pCtr, pTgt) < d);
      //since the pairwise distance is less than d, a bounding box from ctr with d should contain pTgt.
      Rectangle r = dc().calcBoxByDistFromPt(pCtr, d, ctx, null);
      assertEquals(SpatialRelation.CONTAINS,r.relate(pTgt));
      checkBBox(pCtr,d);
    }

    assertEquals("0 dist, horiz line",
        -45,dc().calcBoxByDistFromPt_yHorizAxisDEG(ctx.makePoint(-180, -45), 0, ctx),0);

    double MAXDIST = (double) 180 * DEG_TO_KM;
    checkBBox(ctx.makePoint(0,0), MAXDIST);
    checkBBox(ctx.makePoint(0,0), MAXDIST *0.999999);
    checkBBox(ctx.makePoint(0,0),0);
    checkBBox(ctx.makePoint(0,0),0.000001);
    checkBBox(ctx.makePoint(0,90),0.000001);
    checkBBox(ctx.makePoint(-32.7,-5.42),9829);
    checkBBox(ctx.makePoint(0,90-20), (double) 20 * DEG_TO_KM);
    {
      double d = 0.010;//10m
      checkBBox(ctx.makePoint(0,90- (d + 0.001) * KM_TO_DEG),d);
    }

    for (int T = 0; T < 100; T++) {
      double lat = -90 + randomDouble()*180;
      double lon = -180 + randomDouble()*360;
      Point ctr = ctx.makePoint(lon, lat);
      double dist = MAXDIST*randomDouble();
      checkBBox(ctr, dist);
    }

  }

  private void checkBBox(Point ctr, double distKm) {
    String msg = "ctr: "+ctr+" distKm: "+distKm;
    double dist = distKm * KM_TO_DEG;

    Rectangle r = dc().calcBoxByDistFromPt(ctr, dist, ctx, null);
    double horizAxisLat = dc().calcBoxByDistFromPt_yHorizAxisDEG(ctr, dist, ctx);
    if (!Double.isNaN(horizAxisLat))
      assertTrue(r.relateYRange(horizAxisLat, horizAxisLat).intersects());

    //horizontal
    if (r.getWidth() >= 180) {
      double deg = dc().distance(ctr, r.getMinX(), r.getMaxY() == 90 ? 90 : -90);
      double calcDistKm = deg * DEG_TO_KM;
      assertTrue(msg, calcDistKm <= distKm + EPS);
      //horizAxisLat is meaningless in this context
    } else {
      Point tPt = findClosestPointOnVertToPoint(r.getMinX(), r.getMinY(), r.getMaxY(), ctr);
      double calcDistKm = dc().distance(ctr, tPt) * DEG_TO_KM;
      assertEquals(msg, distKm, calcDistKm, EPS);
      assertEquals(msg, tPt.getY(), horizAxisLat, EPS);
    }

    //vertical
    double topDistKm = dc().distance(ctr, ctr.getX(), r.getMaxY()) * DEG_TO_KM;
    if (r.getMaxY() == 90)
      assertTrue(msg, topDistKm <= distKm + EPS);
    else
      assertEquals(msg, distKm, topDistKm, EPS);
    double botDistKm = dc().distance(ctr, ctr.getX(), r.getMinY()) * DEG_TO_KM;
    if (r.getMinY() == -90)
      assertTrue(msg, botDistKm <= distKm + EPS);
    else
      assertEquals(msg, distKm, botDistKm, EPS);
  }

  private Point findClosestPointOnVertToPoint(double lon, double lowLat, double highLat, Point ctr) {
    //A binary search algorithm to find the point along the vertical lon between lowLat & highLat that is closest
    // to ctr, and returns the distance.
    double midLat = (highLat - lowLat)/2 + lowLat;
    double midLatDist = ctx.getDistCalc().distance(ctr,lon,midLat);
    for(int L = 0; L < 100 && (highLat - lowLat > 0.001|| L < 20); L++) {
      boolean bottom = (midLat - lowLat > highLat - midLat);
      double newMid = bottom ? (midLat - lowLat)/2 + lowLat : (highLat - midLat)/2 + midLat;
      double newMidDist = ctx.getDistCalc().distance(ctr,lon,newMid);
      if (newMidDist < midLatDist) {
        if (bottom) {
          highLat = midLat;
        } else {
          lowLat = midLat;
        }
        midLat = newMid;
        midLatDist = newMidDist;
      } else {
        if (bottom) {
          lowLat = newMid;
        } else {
          highLat = newMid;
        }
      }
    }
    return ctx.makePoint(lon,midLat);
  }

  @Test
  public void testDistCalcPointOnBearing_cartesian() {
    ctx = new SpatialContext(false);
    EPS = 10e-6;//tighter epsilon (aka delta)
    for(int i = 0; i < 1000; i++) {
      testDistCalcPointOnBearing(randomInt(100));
    }
  }

  @Test
  public void testDistCalcPointOnBearing_geo() {
    //The haversine formula has a higher error if the points are near antipodal. We adjust EPS tolerance for this case.
    //TODO Eventually we should add the Vincenty formula for improved accuracy, or try some other cleverness.

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
    double maxDistKm = (double) 180 * DEG_TO_KM;
    for(int i = 0; i < 1000; i++) {
      int distKm = randomInt((int) maxDistKm);
      EPS = (distKm < maxDistKm*0.75 ? 10e-6 : 10e-3);
      testDistCalcPointOnBearing(distKm);
    }
  }

  private void testDistCalcPointOnBearing(double distKm) {
    for(int angDEG = 0; angDEG < 360; angDEG += randomIntBetween(1,20)) {
      Point c = ctx.makePoint(
              DistanceUtils.normLonDEG(randomInt(359)),
              randomIntBetween(-90,90));

      //0 distance means same point
      Point p2 = dc().pointOnBearing(c, 0, angDEG, ctx, null);
      assertEquals(c,p2);

      p2 = dc().pointOnBearing(c, distKm * KM_TO_DEG, angDEG, ctx, null);
      double calcDistKm = dc().distance(c, p2) * DEG_TO_KM;
      assertEqualsRatio(distKm, calcDistKm);
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
      assertEquals("input "+pair[0], pair[1], DistanceUtils.normLatDEG(pair[0]), 0);
    }

    for(int i = -1000; i < 1000; i += randomInt(9)*10) {
      double d = DistanceUtils.normLatDEG(i);
      assertTrue(i + " " + d, d >= -90 && d <= 90);
    }
  }

  @Test
  public void testNormLon() {
    double[][] lons = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-180,-180},{180,+180},{0,0}, {-190,170},{181,-179},
        {-180-360,-180},{-180-720,-180},
        {180+360,+180},{180+720,+180}};
    for (double[] pair : lons) {
      assertEquals("input " + pair[0], pair[1], DistanceUtils.normLonDEG(pair[0]), 0);
    }

    for(int i = -1000; i < 1000; i += randomInt(9)*10) {
      double d = DistanceUtils.normLonDEG(i);
      assertTrue(i + " " + d, d >= -180 && d <= 180);
    }
  }

  @Test
  public void assertDistanceConversion() {
    assertDistanceConversion(0);
    assertDistanceConversion(500);
    assertDistanceConversion(DistanceUtils.EARTH_MEAN_RADIUS_KM);
  }

  private void assertDistanceConversion(double dist) {
    double radius = DistanceUtils.EARTH_MEAN_RADIUS_KM;
    //test back & forth conversion for both
    double distRAD = DistanceUtils.dist2Radians(dist, radius);
    assertEquals(dist, DistanceUtils.radians2Dist(distRAD, radius), EPS);
    double distDEG = DistanceUtils.dist2Degrees(dist, radius);
    assertEquals(dist, DistanceUtils.degrees2Dist(distDEG, radius), EPS);
    //test across rad & deg
    assertEquals(distDEG,DistanceUtils.toDegrees(distRAD),EPS);
    //test point on bearing
    assertEquals(
        DistanceUtils.pointOnBearingRAD(0, 0, DistanceUtils.dist2Radians(dist, radius), DistanceUtils.DEG_90_AS_RADS, ctx, new PointImpl(0, 0, ctx)).getX(),
        distRAD, 10e-5);
  }

  private Point pLL(double lat, double lon) {
    return ctx.makePoint(lon,lat);
  }

  @Test
  public void testArea() {
    double radius = DistanceUtils.EARTH_MEAN_RADIUS_KM * KM_TO_DEG;
    //surface of a sphere is 4 * pi * r^2
    final double earthArea = 4 * Math.PI * radius * radius;

    Circle c = ctx.makeCircle(randomIntBetween(-180,180), randomIntBetween(-90,90),
            180);//180 means whole earth
    assertEquals(earthArea, c.getArea(ctx), 1.0);
    assertEquals(earthArea, ctx.getWorldBounds().getArea(ctx), 1.0);

    //now check half earth
    Circle cHalf = ctx.makeCircle(c.getCenter(), 90);
    assertEquals(earthArea/2, cHalf.getArea(ctx), 1.0);

    //circle with same radius at +20 lat with one at -20 lat should have same area as well as bbox with same area
    Circle c2 = ctx.makeCircle(c.getCenter(), 30);
    Circle c3 = ctx.makeCircle(c.getCenter().getX(), 20, 30);
    assertEquals(c2.getArea(ctx), c3.getArea(ctx), 0.01);
    Circle c3Opposite = ctx.makeCircle(c.getCenter().getX(), -20, 30);
    assertEquals(c3.getArea(ctx), c3Opposite.getArea(ctx), 0.01);
    assertEquals(c3.getBoundingBox().getArea(ctx), c3Opposite.getBoundingBox().getArea(ctx), 0.01);

    //small shapes near the equator should have similar areas to euclidean rectangle
    Rectangle smallRect = ctx.makeRectangle(0, 1, 0, 1);
    assertEquals(1.0, smallRect.getArea(null), 0.0);
    double smallDelta = smallRect.getArea(null) - smallRect.getArea(ctx);
    assertTrue(smallDelta > 0 && smallDelta < 0.0001);

    Circle smallCircle = ctx.makeCircle(0,0,1);
    smallDelta = smallCircle.getArea(null) - smallCircle.getArea(ctx);
    assertTrue(smallDelta > 0 && smallDelta < 0.0001);

    //bigger, but still fairly similar
    //c2 = ctx.makeCircle(c.getCenter(), 30);
    double areaRatio = c2.getArea(null) / c2.getArea(ctx);
    assertTrue(areaRatio > 1 && areaRatio < 1.1);
  }

}
