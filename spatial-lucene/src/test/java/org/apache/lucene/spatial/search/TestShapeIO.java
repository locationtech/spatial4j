package org.apache.lucene.spatial.search;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;


/**
 */
public class TestShapeIO extends TestCase
{
  public void testShapeIO() throws Exception
  {
    JTSShapeIO reader = new JTSShapeIO();

    // Simple Point
    Shape s = reader.readShape( "10 20" );
    Point p = (Point)s;
    assertEquals( 10.0, p.getX() );
    assertEquals( 20.0, p.getY() );
    byte[] buff = reader.toBytes( s );

    s = reader.readShape( buff, 0, buff.length );
    p = (Point)s;
    assertEquals( 10.0, p.getX() );
    assertEquals( 20.0, p.getY() );

    // BBOX
    s = reader.readShape( "-10 -20 10 20" );
    BBox b = (BBox)s;
    assertEquals( -10.0, b.getMinX() );
    assertEquals( -20.0, b.getMinY() );
    assertEquals(  10.0, b.getMaxX() );
    assertEquals(  20.0, b.getMaxY() );
    buff = reader.toBytes( s );
    s = reader.readShape( buff, 0, buff.length );
    b = (BBox)s;
    assertEquals( -10.0, b.getMinX() );
    assertEquals( -20.0, b.getMinY() );
    assertEquals(  10.0, b.getMaxX() );
    assertEquals(  20.0, b.getMaxY() );
  }
}
