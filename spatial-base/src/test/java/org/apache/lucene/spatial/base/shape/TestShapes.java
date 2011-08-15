package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.ArcDistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.distance.HaversineDistanceCalculator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author dsmiley
 */
public class TestShapes {

  private static final double DELTA = 0.0001;
  SpatialContext ctx = new SimpleSpatialContext(DistanceUnits.KILOMETERS);
  DistanceCalculator DC = new HaversineDistanceCalculator(DistanceUnits.KILOMETERS.earthRadius());

  @Test
  public void testRectangle() {
    double[] lons = new double[]{0,45,175,180, -45,-175,-180};//minX
    for (double lon : lons) {
      double[] lonWs = new double[]{0,20,200,360};//width
      for (double lonW : lonWs) {
        if (lonW == 360 && lon != -180)//360 is only supported properly from -180
          continue;
        Rectangle r = ctx.makeRect(lon,w(lon+lonW),-10,10);
        String msg = r.toString();
        assertEquals(msg,lonW != 0,r.hasSize());
        assertEqualsPct(msg, 20, r.getHeight());
        assertEqualsPct(msg, lonW, r.getWidth());
        Point center = r.getCenter();
        msg += " ctr:"+center;
        //System.out.println(msg);
        assertEquals(msg,IntersectCase.CONTAINS,r.intersect(center,ctx));
        double dCorner = calcDist(center, r.getMaxX(),r.getMaxY());//UR
        assertTrue(dCorner > 0);
        assertEqualsPct(msg, dCorner, calcDist(center, r.getMaxX(), r.getMinY()));//LR
        assertEqualsPct(msg, dCorner, calcDist(center, r.getMinX(), r.getMaxY()));//UL
        assertEqualsPct(msg, dCorner, calcDist(center, r.getMinX(), r.getMinY()));//LL
      }
    }
  }

  void assertEqualsPct(String msg, double expected, double actuals) {
    double delta = expected * 0.07;
    //System.out.println(delta);
    assertEquals(msg,expected,actuals, delta);
  }

  private double calcDist(Point center, double x, double y) {

    return DC.calculate(center, x, y);
  }

  /** normalize longitude */
  private double w(double lon) {
    if (lon > 180)
      return w(lon - 360);
    if (lon < -180)
      return w(lon + 360);
    return lon;
  }
}
