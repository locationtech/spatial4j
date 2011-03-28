package org.apache.lucene.spatial.base;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;



/**
 */
public class TestSpatialArgs extends TestCase
{
  public void checkSimpleArgs( ShapeIO reader) throws Exception
  {
    String arg = SpatialOperation.IsWithin+"(-10 -20 10 20) cache=true score=false";
    SpatialArgs out = SpatialArgs.parse(arg, reader);
    assertEquals(SpatialOperation.IsWithin, out.getOperation());
    assertTrue(out.isCacheable());
    assertFalse(out.isCalculateScore());
    BBox bounds = (BBox) out.getShape();
    assertEquals( -10.0, bounds.getMinX() );
    assertEquals( 10.0, bounds.getMaxX() );

    // Disjoint should not be scored
    arg = SpatialOperation.IsDisjointTo+" (-10 10 -20 20) score=true";
    out = SpatialArgs.parse(arg, reader);
    assertEquals(SpatialOperation.IsDisjointTo, out.getOperation());
    assertFalse(out.isCalculateScore());

    try {
      SpatialArgs.parse( SpatialOperation.IsDisjointTo+"[ ]", reader);
      fail( "spatial operations need args");
    }
    catch( Exception ex ) {}

    try {
      SpatialArgs.parse("XXXX(-10 10 -20 20)", reader);
      fail( "unknown operation!");
    }
    catch( Exception ex ) { }

    // Check distance
    arg = SpatialOperation.Distance+"(1 2) min=2.3 max=4.5";
    out = SpatialArgs.parse(arg, reader);
    assertEquals(SpatialOperation.Distance, out.getOperation());
    assertTrue(out.getShape() instanceof Point);
    assertEquals(2.3, out.getMin(), 0D);
    assertEquals(4.5, out.getMax(), 0D);
  }

  public void testSimpleArgs() throws Exception
  {
    checkSimpleArgs( new SimpleShapeIO() );
  }

  public void testJTSArgs() throws Exception
  {
    ShapeIO reader = new JTSShapeIO();
    checkSimpleArgs( reader );

    // now check the complex stuff...
  }
}
