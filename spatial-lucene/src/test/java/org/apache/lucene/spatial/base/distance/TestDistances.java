package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class TestDistances {

  private final SpatialContext ctx = new SimpleSpatialContext(DistanceUnits.KILOMETERS);
  private final DistanceCalculator DC = new HaversineDistanceCalculator(DistanceUtils.EARTH_MEAN_RADIUS_KM);

  @Test
  public void testSomeDistances() {
    //See to veryify: from http://www.movable-type.co.uk/scripts/latlong.html
    Point ctr = pLL(0,100);
    assertEquals(11100,DC.calculate(ctr,pLL(10,0)),40);
    assertEquals(11100,DC.calculate(ctr,pLL(10,-160)),40);
  }

  @Test
  public void testHaversineBBox() {
    double[] lats = new double[]{0, 85, 90, -85, -90};
    for (double lat : lats) {
      double[] lons = new double[]{0, 175, 180, -175, -180};
      for (double lon : lons) {
        double dist5Deg = DC.calculate(pLL(85,0),pLL(90,0));
        double distShort = dist5Deg / 2;
        double distMedium = dist5Deg * 2;
        //double distLong = DC.calculate(pLL(-45,0),pLL(50,0));//100 degrees (more than 90)
        double latA = Math.abs(lat);
        double lonA = Math.abs(lon);

        Point center = pLL(lat, lon);
        //--distShort
        Rectangle r = DC.calcBoxByDistFromPt(center,distShort,ctx);
        String msg = r+" ctr:"+center;

        checkR(msg, r, center);
        assertEquals(latA == 90, spans(r));
        //TODO
      }
    }
  }

  @Test
  public void testNormLat() {
    double[][] lats = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-90,-90},{90,90},{0,0}, {-100,-80},
        {-90-180,90},{-90-360,-90},{90+180,-90},{90+360,90}};
    for (double[] pair : lats) {
      assertEquals("input "+pair[0],pair[1],ctx.normY(pair[0]),0);
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
  }

  private void checkR(String msg, Rectangle r, Point center) {
    if (!spans(r)) {
      assertEquals(msg,DistanceUtils.normLonDeg(center.getX()), r.getCenter().getX(), 0.0001);
    }
    assertEquals(msg, IntersectCase.CONTAINS, ctx.getWorldBounds().intersect(r, ctx));
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
