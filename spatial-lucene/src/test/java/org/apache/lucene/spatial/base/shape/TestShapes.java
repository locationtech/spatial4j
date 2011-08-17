package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author dsmiley
 */
public class TestShapes {

  @Test
  public void testGeoRectangle() {
    SpatialContext ctx = new SimpleSpatialContext(DistanceUnits.KILOMETERS);
    double[] lons = new double[]{0,45,175,180, -45,-175};//minX
    for (double lon : lons) {
      double[] lonWs = new double[]{0,20,180,200};//width
      for (double lonW : lonWs) {
        testRectangle(lon, lonW, 0, ctx);
        testRectangle(lon, lonW, 20, ctx);
      }
    }
    //The only way to officially support complete longitude wrap-around is via western longitude = -180. We can't
    // support any point because 0 is undifferentiated in sign.
    testRectangle(-180, 360, 0, ctx);
    testRectangle(-180, 360, 20, ctx);
  }

  private void testRectangle(double x, double width, int height, SpatialContext ctx) {
    double maxX = x + width;
    if (ctx.isGeo())
      maxX = DistanceUtils.normLonDeg(maxX);
    Rectangle r = ctx.makeRect(x, maxX, -height / 2, height / 2);
    String msg = r.toString();

    assertEquals(msg, width != 0 && height != 0, r.hasSize());
    assertEquals(msg, width != 0 && height != 0, r.getArea() > 0);
    assertEqualsPct(msg, height, r.getHeight());
    assertEqualsPct(msg, width, r.getWidth());
    Point center = r.getCenter();
    msg += " ctr:"+center;
    //System.out.println(msg);
    assertEquals(msg, IntersectCase.CONTAINS,r.intersect(center, ctx));
    DistanceCalculator dc = ctx.getDistanceCalculator();
    double dCorner = dc.calculate(center, r.getMaxX(), r.getMaxY());//UR

    assertEquals(msg,width != 0 || height != 0, dCorner != 0);
    if (ctx.isGeo())
      assertTrue(msg,dCorner >= 0);
    assertEqualsPct(msg, dCorner, dc.calculate(center, r.getMaxX(), r.getMinY()));//LR
    assertEqualsPct(msg, dCorner, dc.calculate(center, r.getMinX(), r.getMaxY()));//UL
    assertEqualsPct(msg, dCorner, dc.calculate(center, r.getMinX(), r.getMinY()));//LL
  }

  @Test
  public void testSimpleRectangle() {
    SpatialContext ctx = new SimpleSpatialContext(DistanceUnits.EUCLIDEAN);
    double[] minXs = new double[]{-1000,-360,-180,-20,0,20,180,1000};
    for (double minX : minXs) {
      double[] widths = new double[]{0,10,180,360,400};
      for (double width : widths) {
        testRectangle(minX, width, 0, ctx);
        testRectangle(minX, width, 20, ctx);
      }
    }
  }

  void assertEqualsPct(String msg, double expected, double actual) {
    double delta = Math.abs(expected * 0.07);// TODO 7%!  I don't like that having it any smaller breaks. Why?
    //System.out.println(delta);
    assertEquals(msg,expected,actual, delta);
  }

}
