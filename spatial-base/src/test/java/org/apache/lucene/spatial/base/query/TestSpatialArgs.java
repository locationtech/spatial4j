package org.apache.lucene.spatial.base.query;

import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 */
public class TestSpatialArgs {

  @Test
  public void checkSimpleArgs(ShapeIO reader) {
    SpatialArgsParser parser = new SpatialArgsParser();
    
    String arg = SpatialOperation.IsWithin + "(-10 -20 10 20) cache=true score=false";
    SpatialArgs out = parser.parse(arg, reader);
    assertEquals(SpatialOperation.IsWithin, out.getOperation());
    assertTrue(out.isCacheable());
    assertFalse(out.isCalculateScore());
    BBox bounds = (BBox) out.getShape();
    assertEquals(-10.0, bounds.getMinX(), 0D);
    assertEquals(10.0, bounds.getMaxX(), 0D);

    // Disjoint should not be scored
    arg = SpatialOperation.IsDisjointTo + " (-10 10 -20 20) score=true";
    out = parser.parse(arg, reader);
    assertEquals(SpatialOperation.IsDisjointTo, out.getOperation());
    assertFalse(out.isCalculateScore());

    try {
      parser.parse(SpatialOperation.IsDisjointTo + "[ ]", reader);
      fail("spatial operations need args");
    }
    catch (Exception ex) {
    }

    try {
      parser.parse("XXXX(-10 10 -20 20)", reader);
      fail("unknown operation!");
    }
    catch (Exception ex) {
    }

    // Check distance
    arg = SpatialOperation.Distance + "(1 2) min=2.3 max=4.5";
    out = parser.parse(arg, reader);
    assertEquals(SpatialOperation.Distance, out.getOperation());
    assertTrue(out.getShape() instanceof Point);
    assertEquals(2.3, out.getMin(), 0D);
    assertEquals(4.5, out.getMax(), 0D);
  }

  @Test
  public void testSimpleArgs() throws Exception {
    checkSimpleArgs(new SimpleShapeIO());
  }

  @Test
  public void testJTSArgs() throws Exception {
    ShapeIO reader = new JTSShapeIO();
    checkSimpleArgs(reader);

    // now check the complex stuff...
  }
}
