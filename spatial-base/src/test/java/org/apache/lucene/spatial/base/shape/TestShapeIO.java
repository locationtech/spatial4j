package org.apache.lucene.spatial.base.shape;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.spatial.base.shape.jts.JtsEnvelope;
import org.apache.lucene.spatial.base.shape.jts.JtsGeometry;
import org.apache.lucene.spatial.base.shape.jts.JtsPoint2D;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.junit.Assert;
import org.junit.Test;


/**
 */
public class TestShapeIO {


  @Test
  public void testShapesImplementEquals() {

    Class<Shape>[] classes = new Class[] {
      JtsEnvelope.class,  
      JtsGeometry.class,  
      JtsPoint2D.class,  
//      Point2D.class,  
//      Rectangle.class,  
    };

    for( Class<Shape> clazz : classes ) {
      try {
        clazz.getDeclaredMethod( "equals", Object.class );
      } catch (Exception e) {
        Assert.fail( "Shapes need to define 'equals' : " + clazz.getName() );
      }
      try {
        clazz.getDeclaredMethod( "hashCode" );
      } catch (Exception e) {
        Assert.fail( "Shapes need to define 'hashCode' : " + clazz.getName() );
      }
    }
  }

  static interface WriteReader {
    Shape writeThenRead( Shape s ) throws IOException;
  };


  public void checkBasicShapeIO( AbstractShapeIO reader, WriteReader help ) throws Exception {

    // Simple Point
    Shape s = reader.readShape("10 20");
    Point p = (Point) s;
    assertEquals(10.0, p.getX(), 0D);
    assertEquals(20.0, p.getY(), 0D);
    p = (Point) help.writeThenRead(s);
    assertEquals(10.0, p.getX(), 0D);
    assertEquals(20.0, p.getY(), 0D);
    Assert.assertFalse( s.hasArea() );

    // BBOX
    s = reader.readShape("-10 -20 10 20");
    BBox b = (BBox) s;
    assertEquals(-10.0, b.getMinX(), 0D);
    assertEquals(-20.0, b.getMinY(), 0D);
    assertEquals(10.0, b.getMaxX(), 0D);
    assertEquals(20.0, b.getMaxY(), 0D);
    b = (BBox) help.writeThenRead(s);
    assertEquals(-10.0, b.getMinX(), 0D);
    assertEquals(-20.0, b.getMinY(), 0D);
    assertEquals(10.0, b.getMaxX(), 0D);
    assertEquals(20.0, b.getMaxY(), 0D);
    Assert.assertTrue( s.hasArea() );

    // Point/Distance
    s = reader.readShape("PointDistance( 1.23 4.56 distance=7.89)");
    PointDistanceShape circle = (PointDistanceShape)s;
    assertEquals(1.23, circle.getPoint().getX(), 0D);
    assertEquals(4.56, circle.getPoint().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
    assertEquals(reader.units.earthRadius(), circle.getRadius(), 0D);
    Assert.assertTrue( s.hasArea() );

    s = reader.readShape("PointDistance( 1.23  4.56 d=7.89 )");
    circle = (PointDistanceShape)s;
    assertEquals(1.23, circle.getPoint().getX(), 0D);
    assertEquals(4.56, circle.getPoint().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
    assertEquals(reader.units.earthRadius(), circle.getRadius(), 0D);
  }


  @Test
  public void testSimpleShapeIO() throws Exception {
    final SimpleShapeIO io = new SimpleShapeIO();
    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });
  }

  @Test
  public void testJtsShapeIO() throws Exception {
    final JtsShapeIO io = new JtsShapeIO();

    // String read/write
    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) throws IOException {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });

    // Binary read/write
    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) throws IOException {
        byte[] buff = io.toBytes(s);
        return io.readShape(buff, 0, buff.length);
      }
    });
    
    // Line does not have area
    String wkt = "LINESTRING(-120.1484375 26.98046875,-119.62109375 39.4609375,-107.140625 50.0078125,-92.0234375 54.75390625)";
    Shape shape = io.readShape( wkt );
    Assert.assertFalse( shape.hasArea() );
  }
}
