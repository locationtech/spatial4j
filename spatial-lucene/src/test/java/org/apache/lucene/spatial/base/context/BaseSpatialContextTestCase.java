package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.*;
import org.apache.lucene.spatial.base.shape.simple.CircleImpl;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.spatial.base.shape.simple.RectangleImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 */
public abstract class BaseSpatialContextTestCase {

  protected abstract SpatialContext getSpatialContext();

  public static void checkArgParser(SpatialContext ctx) {
    SpatialArgsParser parser = new SpatialArgsParser();

    String arg = SpatialOperation.IsWithin + "(-10 -20 10 20)";
    SpatialArgs out = parser.parse(arg, ctx);
    assertEquals(SpatialOperation.IsWithin, out.getOperation());
    Rectangle bounds = (Rectangle) out.getShape();
    assertEquals(-10.0, bounds.getMinX(), 0D);
    assertEquals(10.0, bounds.getMaxX(), 0D);

    // Disjoint should not be scored
    arg = SpatialOperation.IsDisjointTo + " (-10 10 -20 20)";
    out = parser.parse(arg, ctx);
    assertEquals(SpatialOperation.IsDisjointTo, out.getOperation());

    try {
      parser.parse(SpatialOperation.IsDisjointTo + "[ ]", ctx);
      fail("spatial operations need args");
    }
    catch (Exception ex) {
    }

    try {
      parser.parse("XXXX(-10 10 -20 20)", ctx);
      fail("unknown operation!");
    }
    catch (Exception ex) {
    }
  }

  public static void checkShapesImplementEquals( Class<Shape>[] classes ) {

    for( Class<Shape> clazz : classes ) {
      try {
        clazz.getDeclaredMethod( "equals", Object.class );
      } catch (Exception e) {
        Assert.fail( "Shape needs to define 'equals' : " + clazz.getName() );
      }
      try {
        clazz.getDeclaredMethod( "hashCode" );
      } catch (Exception e) {
        Assert.fail( "Shape needs to define 'hashCode' : " + clazz.getName() );
      }
    }
  }

  public static interface WriteReader {
    Shape writeThenRead( Shape s ) throws IOException;
  };


  public static void checkBasicShapeIO( SpatialContext ctx, WriteReader help ) throws Exception {

    // Simple Point
    Shape s = ctx.readShape("10 20");
    Point p = (Point) s;
    assertEquals(10.0, p.getX(), 0D);
    assertEquals(20.0, p.getY(), 0D);
    p = (Point) help.writeThenRead(s);
    assertEquals(10.0, p.getX(), 0D);
    assertEquals(20.0, p.getY(), 0D);
    Assert.assertFalse(s.hasArea());

    // BBOX
    s = ctx.readShape("-10 -20 10 20");
    Rectangle b = (Rectangle) s;
    assertEquals(-10.0, b.getMinX(), 0D);
    assertEquals(-20.0, b.getMinY(), 0D);
    assertEquals(10.0, b.getMaxX(), 0D);
    assertEquals(20.0, b.getMaxY(), 0D);
    b = (Rectangle) help.writeThenRead(s);
    assertEquals(-10.0, b.getMinX(), 0D);
    assertEquals(-20.0, b.getMinY(), 0D);
    assertEquals(10.0, b.getMaxX(), 0D);
    assertEquals(20.0, b.getMaxY(), 0D);
    Assert.assertTrue(s.hasArea());

    // Point/Distance
    s = ctx.readShape("Circle( 1.23 4.56 distance=7.89)");
    CircleImpl circle = (CircleImpl)s;
    assertEquals(1.23, circle.getCenter().getX(), 0D);
    assertEquals(4.56, circle.getCenter().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
    Assert.assertTrue(s.hasArea());

    s = ctx.readShape("Circle( 1.23  4.56 d=7.89 )");
    circle = (CircleImpl) s;
    assertEquals(1.23, circle.getCenter().getX(), 0D);
    assertEquals(4.56, circle.getCenter().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
  }


  public static void checkBBoxIntersection( SpatialContext ctx ) {

    Rectangle big = ctx.makeRect(0, 100, 0, 100);
    Rectangle rr0 = ctx.makeRect(25, 75, 25, 75);
    Rectangle rr1 = ctx.makeRect(120, 150, 0, 100);
    Rectangle rr2 = ctx.makeRect(-1, 50, 0, 50);

    assertEquals(IntersectCase.CONTAINS, big.intersect(rr0, ctx));
    assertEquals(IntersectCase.WITHIN, rr0.intersect(big, ctx));
    assertEquals(IntersectCase.OUTSIDE, big.intersect(rr1, ctx));
    assertEquals(IntersectCase.OUTSIDE, rr1.intersect(big, ctx));
    assertEquals(IntersectCase.INTERSECTS, rr2.intersect(big, ctx));
    assertEquals(IntersectCase.INTERSECTS, big.intersect(rr2, ctx));

    Point p1 = ctx.makePoint(1000, 20);
    Point p2 = ctx.makePoint(50, 50);
    assertEquals(IntersectCase.OUTSIDE, p1.intersect(big, ctx));
    assertEquals(IntersectCase.WITHIN, p2.intersect(big, ctx));
  }

  //--------------------------------------------------------------
  // Actual tests
  //--------------------------------------------------------------

  @Test
  public void testArgsParser() throws Exception {
    checkArgParser( getSpatialContext() );
  }

  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    checkShapesImplementEquals( new Class[] {
      PointImpl.class,
      CircleImpl.class,
      RectangleImpl.class,
      MultiShape.class,
    });
  }

  @Test
  public void testSimpleShapeIO() throws Exception {
    final SpatialContext io =  getSpatialContext();
    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });
  }

  @Test
  public void testSimpleIntersection() throws Exception {
    checkBBoxIntersection(getSpatialContext());
  }
}
