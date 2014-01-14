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

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.exception.InvalidShapeException;
import org.junit.Test;

import java.util.Arrays;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;


public class TestShapesGeo extends AbstractTestShapes {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {

    //TODO ENABLE LawOfCosines WHEN WORKING
    //DistanceCalculator distCalcL = new GeodesicSphereDistCalc.Haversine(units.earthRadius());//default
    final DistanceCalculator distCalcH = new GeodesicSphereDistCalc.Haversine();//default
    final DistanceCalculator distCalcV = new GeodesicSphereDistCalc.Vincenty();
    return Arrays.asList($$(
        $(new SpatialContextFactory(){{geo = true; distCalc = new RoundingDistCalc(distCalcH);}}.newSpatialContext()),
        $(new SpatialContextFactory(){{geo = true; distCalc = new RoundingDistCalc(distCalcV);}}.newSpatialContext()),
        $(new JtsSpatialContextFactory(){{geo = true; distCalc = new RoundingDistCalc(distCalcH);}}.newSpatialContext()))
    );
  }

  public TestShapesGeo(SpatialContext ctx) {
    super(ctx);
  }

  private static double degToKm(double deg) {
    return DistanceUtils.degrees2Dist(deg, DistanceUtils.EARTH_MEAN_RADIUS_KM);
  }

  private static double kmToDeg(double km) {
    return DistanceUtils.dist2Degrees(km, DistanceUtils.EARTH_MEAN_RADIUS_KM);
  }

  @Test
  public void testGeoRectangle() {
    double v = 200 * (randomBoolean() ? -1 : 1);
    try { ctx.makeRectangle(v,0,0,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,v,0,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,v,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,0,v); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,10,-10); fail(); } catch (InvalidShapeException e) {}

    //test some relateXRange
    //    opposite +/- 180
    assertEquals(INTERSECTS,  ctx.makeRectangle(170, 180, 0, 0).relateXRange(-180, -170));
    assertEquals(INTERSECTS,  ctx.makeRectangle(-90, -45, 0, 0).relateXRange(-45, -135));
    assertEquals(CONTAINS, ctx.getWorldBounds().relateXRange(-90, -135));
    //point on edge at dateline using opposite +/- 180
    assertEquals(CONTAINS, ctx.makeRectangle(170, 180, 0, 0).relate(ctx.makePoint(-180, 0)));

    //test 180 becomes -180 for non-zero width rectangle
    assertEquals(ctx.makeRectangle(-180, -170, 0, 0),ctx.makeRectangle(180, -170, 0, 0));
    assertEquals(ctx.makeRectangle(170, 180, 0, 0),ctx.makeRectangle(170, -180, 0, 0));

    double[] lons = new double[]{0,45,160,180,-45,-175, -180};//minX
    for (double lon : lons) {
      double[] lonWs = new double[]{0,20,180,200,355, 360};//width
      for (double lonW : lonWs) {
        if (lonW == 360 && lon != -180)
          continue;
        testRectangle(lon, lonW, 0, 0);
        testRectangle(lon, lonW, -10, 10);
        testRectangle(lon, lonW, 80, 10);//polar cap
        testRectangle(lon, lonW, -90, 180);//full lat range
      }
    }

    TestShapes2D.testCircleReset(ctx);

    //Test geo rectangle intersections
    testRectIntersect();

    //Test buffer
    assertEquals(ctx.makeRectangle(-10, 10, -10, 10), ctx.makeRectangle(0, 0, 0, 0).getBuffered(10, ctx));
    for (int i = 0; i < atLeast(100); i++) {
      Rectangle r = randomRectangle(1);
      int buf = randomIntBetween(0, 90);
      Rectangle br = (Rectangle) r.getBuffered(buf, ctx);
      assertRelation(null, CONTAINS, br, r);
      if (r.getWidth() + 2 * buf >= 360)
        assertEquals(360, br.getWidth(), 0.0);
      else
        assertTrue(br.getWidth() - r.getWidth() >= 2 * buf);
      //TODO test more thoroughly; we don't check that we over-buf
    }
    assertTrue(ctx.makeRectangle(0, 10, 0, 89).getBuffered(0.5, ctx).getBoundingBox().getWidth()
        > 11);
  }

  @Test
  public void testGeoCircle() {
    assertEquals("Circle(Pt(x=10.0,y=20.0), d=30.0° 3335.85km)", ctx.makeCircle(10,20,30).toString());

    double v = 200 * (randomBoolean() ? -1 : 1);
    try { ctx.makeCircle(v,0,5); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeCircle(0, v, 5); fail(); } catch (InvalidShapeException e) {}
//    try { ctx.makeCircle(randomIntBetween(-180,180), randomIntBetween(-90,90), v); fail(); }
//    catch (InvalidShapeException e) {}

    //--Start with some static tests that once failed:

    //Bug: numeric edge at pole, fails to init
    ctx.makeCircle(110, -12, 90 + 12);

    //Bug: horizXAxis not in enclosing rectangle, assertion
    ctx.makeCircle(-44,16,106);
    ctx.makeCircle(-36,-76,14);
    ctx.makeCircle(107,82,172);

//TODO need to update this test to be valid
//    {
//      //Bug in which distance was being confused as being in the same coordinate system as x,y.
//      double distDeltaToPole = 0.001;//1m
//      double distDeltaToPoleDEG = ctx.getDistCalc().distanceToDegrees(distDeltaToPole);
//      double dist = 1;//1km
//      double distDEG = ctx.getDistCalc().distanceToDegrees(dist);
//      Circle c = ctx.makeCircle(0,90-distDeltaToPoleDEG-distDEG,dist);
//      Rectangle cBBox = c.getBoundingBox();
//      Rectangle r = ctx.makeRect(cBBox.getMaxX()*0.99,cBBox.getMaxX()+1,c.getCenter().getY(),c.getCenter().getY());
//      assertEquals(INTERSECTS,c.getBoundingBox().relate(r));
//      assertEquals("dist != xy space",INTERSECTS,c.relate(r));//once failed here
//    }

    //These two are related to a circle being on-edge with another shape
    //assertEquals("?", INTERSECTS, ctx.makeCircle(156, -70, 20).relate(ctx.makeRectangle(-62, -52, -90, -90)));
    //Pt(x=-52.24150368914137,y=-90.0)
    //assertEquals("?", DISJOINT, ctx.makeCircle(156, -70, 20).relate(ctx.makePoint(-52, -90)));//pt.x != c.x

    //What is the "correct" result?  Add a DistUtils edge condition check to return a nibble
    // when dist 0 and points not the same?  No; we cancel the assertion failure
    // if the circle touches the rect edge in onAssertFail() instead.
    //assertEquals("0 radius at pole", DISJOINT, ctx.makeCircle(-98, 90, 0).relate(ctx.makePoint(-144,90)));


    assertEquals("bad proportion logic", INTERSECTS, ctx.makeCircle(64, -70, 18).relate(ctx.makeRectangle(46, 116, -86, -62)));

    assertEquals("Both touch pole", INTERSECTS, ctx.makeCircle(-90, 30, 60).relate(ctx.makeRectangle(-24, -16, 14, 90)));

    assertEquals("Spherical cap should contain enclosed band", CONTAINS,
        ctx.makeCircle(0, -90, 30).relate(ctx.makeRectangle(-180, 180, -90, -80)));

    assertEquals("touches pole", INTERSECTS, ctx.makeCircle(0, -88, 2).relate(ctx.makeRectangle(40,60,-90,-86)));

    assertEquals("wrong farthest opp corner", INTERSECTS, ctx.makeCircle(92, 36, 46).relate(ctx.makeRectangle(134,136,32,80)));

    assertEquals("edge rounding issue 2", INTERSECTS, ctx.makeCircle(84, -40, 136).relate(ctx.makeRectangle(-150, -80, 34, 84)));

    assertEquals("edge rounding issue", CONTAINS, ctx.makeCircle(0, 66, 156).relate(ctx.makePoint(0, -90)));

    assertEquals("nudge back circle", CONTAINS, ctx.makeCircle(-150, -90, 122).relate(ctx.makeRectangle(0, -132, 32, 32)));

    assertEquals("wrong estimate", DISJOINT,ctx.makeCircle(-166,59,kmToDeg(5226.2)).relate(ctx.makeRectangle(36, 66, 23, 23)));

    assertEquals("bad CONTAINS (dateline)",INTERSECTS,ctx.makeCircle(56,-50,kmToDeg(12231.5)).relate(ctx.makeRectangle(108, 26, 39, 48)));

    assertEquals("bad CONTAINS (backwrap2)",INTERSECTS,
        ctx.makeCircle(112,-3,91).relate(ctx.makeRectangle(-163, 29, -38, 10)));

    assertEquals("bad CONTAINS (r x-wrap)",INTERSECTS,
        ctx.makeCircle(-139,47,80).relate(ctx.makeRectangle(-180, 180, -3, 12)));

    assertEquals("bad CONTAINS (pwrap)",INTERSECTS,
        ctx.makeCircle(-139,47,80).relate(ctx.makeRectangle(-180, 179, -3, 12)));

    assertEquals("no-dist 1",WITHIN,
        ctx.makeCircle(135,21,0).relate(ctx.makeRectangle(-103, -154, -47, 52)));

    assertEquals("bbox <= >= -90 bug",CONTAINS,
        ctx.makeCircle(-64,-84,124).relate(ctx.makeRectangle(-96, 96, -10, -10)));

    //The horizontal axis line of a geo circle doesn't necessarily pass through c's ctr.
    assertEquals("c's horiz axis doesn't pass through ctr",INTERSECTS,
        ctx.makeCircle(71,-44,40).relate(ctx.makeRectangle(15, 27, -62, -34)));

    assertEquals("pole boundary",INTERSECTS,
        ctx.makeCircle(-100,-12,102).relate(ctx.makeRectangle(143, 175, 4, 32)));

    assertEquals("full circle assert",CONTAINS,
        ctx.makeCircle(-64,32,180).relate(ctx.makeRectangle(47, 47, -14, 90)));

    //--Now proceed with systematic testing:
    assertEquals(ctx.getWorldBounds(), ctx.makeCircle(0,0,180).getBoundingBox());
    //assertEquals(ctx.makeCircle(0,0,180/2 - 500).getBoundingBox());

    double[] theXs = new double[]{-180,-45,90};
    for (double x : theXs) {
      double[] theYs = new double[]{-90,-45,0,45,90};
      for (double y : theYs) {
        testCircle(x, y, 0);
        testCircle(x, y, kmToDeg(500));
        testCircle(x, y, 90);
        testCircle(x, y, 180);
      }
    }

    testCircleIntersect();
  }

}
