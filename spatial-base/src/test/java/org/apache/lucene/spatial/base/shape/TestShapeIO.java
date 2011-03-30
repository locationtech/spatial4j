package org.apache.lucene.spatial.base.shape;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.junit.Assert;
import org.junit.Test;


/**
 */
public class TestShapeIO {


  @Test
  public void testShapesImplementEquals() {

    Class<Shape>[] classes = new Class[] {
     //   Point2D.class
    };

    for( Class<Shape> clazz : classes ) {
      try {
        clazz.getDeclaredMethod( "equals", Object.class );
      } catch (Exception e) {
        Assert.fail( "Shapes need to define 'equals'" );
      }
      try {
        clazz.getDeclaredMethod( "hashCode" );
      } catch (Exception e) {
        Assert.fail( "Shapes need to define 'hashCode'" );
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

    // Point/Distance
    s = reader.readShape("GeoCircle( 1.23 4.56 distance=7.89)");
    GeoCircleShape circle = (GeoCircleShape)s;
    assertEquals(1.23, circle.getPoint().getX(), 0D);
    assertEquals(4.56, circle.getPoint().getY(), 0D);
    assertEquals(7.89, circle.getDistance(), 0D);
    assertEquals(reader.units.earthRadius(), circle.getRadius(), 0D);

    s = reader.readShape("GeoCircle( 1.23  4.56 d=7.89 )");
    circle = (GeoCircleShape)s;
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

    // TODO -- Check WKT...
  }
}
