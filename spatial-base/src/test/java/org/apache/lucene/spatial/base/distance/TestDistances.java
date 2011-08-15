package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

  private void checkR(String msg, Rectangle r, Point center) {
    if (!spans(r)) {
      assertEquals(msg,DistanceUtils.normLonDeg(center.getX()), r.getCenter().getX(), 0.0001);
    }
    assertEquals(msg,IntersectCase.CONTAINS, ctx.getWorldBounds().intersect(r, ctx));
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
