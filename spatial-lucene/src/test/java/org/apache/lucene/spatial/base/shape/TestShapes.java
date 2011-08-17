package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author dsmiley
 */
public class TestShapes {

  protected SpatialContext getGeoContext() {
    return new SimpleSpatialContext(DistanceUnits.KILOMETERS);
  }

  protected SpatialContext getNonGeoContext() {
    return new SimpleSpatialContext(DistanceUnits.EUCLIDEAN);
  }

  @Test
  public void testGeoRectangle() {
    SpatialContext ctx = getGeoContext();
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

  @Test
  public void testSimpleRectangle() {
    SpatialContext ctx = getNonGeoContext();
    double[] minXs = new double[]{-1000,-360,-180,-20,0,20,180,1000};
    for (double minX : minXs) {
      double[] widths = new double[]{0,10,180,360,400};
      for (double width : widths) {
        testRectangle(minX, width, 0, ctx);
        testRectangle(minX, width, 20, ctx);
      }
    }
  }

  private void testRectangle(double x, double width, int height, SpatialContext ctx) {
    double maxX = x + width;
    if (ctx.isGeo())
      maxX = DistanceUtils.normLonDeg(maxX);
    Rectangle r = ctx.makeRect(x, maxX, -height / 2, height / 2);
    String msg = r.toString();

    assertEquals(msg, width != 0 && height != 0, r.hasArea());
    assertEquals(msg, width != 0 && height != 0, r.getArea() > 0);
    assertEqualsPct(msg, height, r.getHeight());
    assertEqualsPct(msg, width, r.getWidth());
    Point center = r.getCenter();
    msg += " ctr:"+center;
    //System.out.println(msg);
    assertIntersect(msg, IntersectCase.CONTAINS, r, center, ctx);
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
  public void testSimpleCircle() {
    SpatialContext ctx = getNonGeoContext();
    double[] minXs = new double[]{-1000,-360,-180,-20,0,20,180,1000};
    for (double minX : minXs) {
      double[] widths = new double[]{0,10,180,360,400};
      for (double width : widths) {
        testCircle(minX, width, 0, ctx);
        testCircle(minX, width, 20/2, ctx);
      }
    }
  }

  private void testCircle(double x, double y, double dist, SpatialContext ctx) {
    Circle c = ctx.makeCircle(x, y, dist);
    assertEquals(c,ctx.makeCircle(ctx.makePoint(x,y),dist));
    String msg = c.toString();
    //System.out.println(msg);

    assertEquals(msg,dist > 0, c.hasArea());
    final Rectangle bbox = c.getBoundingBox();
    assertEquals(msg,dist > 0, bbox.getArea() > 0);
    assertEqualsPct(msg, bbox.getHeight(), dist*2);
    assertTrue(msg,bbox.getWidth() >= dist*2);
    assertIntersect(msg, IntersectCase.CONTAINS, c , c.getCenter(), ctx);
    assertIntersect(msg, IntersectCase.CONTAINS, bbox, c, ctx);
  }

  private void assertIntersect(String msg, IntersectCase expected, Shape a, Shape b, SpatialContext ctx ) {
    IntersectCase sect = a.intersect(b, ctx);
    if (sect == expected)
      return;
    if (expected == IntersectCase.WITHIN || expected == IntersectCase.CONTAINS) {
      if (a.getClass().equals(b.getClass())) // they are the same
        assertEquals(a,b);
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
