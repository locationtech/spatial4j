package org.apache.lucene.spatial.base;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.grid.jts.WKTShapeReader;



/**
 */
public class TestSpatialArgs extends TestCase
{
  public void checkSimpleArgs( ShapeReader reader) throws Exception
  {
    String arg = SpatialOperation.IsWithin+"[-10 -20 10 20] cache=true score=false";
    GeometryArgs out = (GeometryArgs)SpatialArgs.parse(arg, reader);
    assertEquals( SpatialOperation.IsWithin, out.op );
    assertTrue( out.cacheable );
    assertFalse( out.calculateScore );
    BBox bounds = (BBox)out.shape;
    assertEquals( -10.0, bounds.getMinX() );
    assertEquals( 10.0, bounds.getMaxX() );
    
    // Disjoint should not be scored
    arg = SpatialOperation.IsDisjointTo+" [-10 10 -20 20] score=true";
    out = (GeometryArgs)SpatialArgs.parse(arg, reader);
    assertEquals( SpatialOperation.IsDisjointTo, out.op );
    assertFalse( out.calculateScore );
    
    try {
      SpatialArgs.parse( SpatialOperation.IsDisjointTo+"[ ]", reader);
      fail( "spatial operations need args");
    }
    catch( Exception ex ) {}

    try {
      SpatialArgs.parse("XXXX[-10 10 -20 20]", reader);
      fail( "unknown operation!");
    }
    catch( Exception ex ) { }
  }
  
  public void testSimpleArgs() throws Exception
  {
    checkSimpleArgs( new SimpleShapeReader() );
  }

  public void testJTSArgs() throws Exception
  {
    ShapeReader reader = new WKTShapeReader();
    checkSimpleArgs( reader );
    
    // now check the complex stuff...
  }
}
