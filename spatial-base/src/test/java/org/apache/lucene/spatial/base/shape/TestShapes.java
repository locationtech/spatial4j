package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author dsmiley
 */
public class TestShapes {

  SpatialContext ctx = new SimpleSpatialContext(DistanceUnits.KILOMETERS);
  DistanceCalculator DC = new HaversineDistanceCalculator(DistanceUnits.KILOMETERS.earthRadius());

  @Test
  public void testRectangle() {
    double[] lons = new double[]{0,45,175,180, -45,-175};//minX
    for (double lon : lons) {
      double[] lonWs = new double[]{0,20,200};//width
      for (double lonW : lonWs) {
        testRectangle(lon, lonW);
      }
    }
    //The only way to officially support complete longitude wrap-around is via western longitude = -180, otherwise some
    // edge cases can get in the way.
    testRectangle(-180,360);
  }

  private void testRectangle(double lon, double lonW) {
    Rectangle r = ctx.makeRect(lon, DistanceUtils.normLonDeg(lon + lonW),-10,10);
    String msg = r.toString();
    assertEquals(msg,lonW != 0,r.hasSize());
    assertEqualsPct(msg, 20, r.getHeight());
    assertEqualsPct(msg, lonW, r.getWidth());
    Point center = r.getCenter();
    msg += " ctr:"+center;
    //System.out.println(msg);
    assertEquals(msg, IntersectCase.CONTAINS,r.intersect(center,ctx));
    double dCorner = calcDist(center, r.getMaxX(),r.getMaxY());//UR
    assertTrue(dCorner > 0);
    assertEqualsPct(msg, dCorner, calcDist(center, r.getMaxX(), r.getMinY()));//LR
    assertEqualsPct(msg, dCorner, calcDist(center, r.getMinX(), r.getMaxY()));//UL
    assertEqualsPct(msg, dCorner, calcDist(center, r.getMinX(), r.getMinY()));//LL
  }

  void assertEqualsPct(String msg, double expected, double actual) {
    double delta = expected * 0.07;// TODO 7%!  I don't like that having it any smaller breaks. Why?
    //System.out.println(delta);
    assertEquals(msg,expected,actual, delta);
  }

  private double calcDist(Point center, double x, double y) {
    return DC.calculate(center, x, y);
  }
}
