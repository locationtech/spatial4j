package org.apache.lucene.spatial.base.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.jts.JtsSpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.BBox;
import org.junit.Test;


/**
 */
public class TestSpatialArgs {

  public void checkSimpleArgs(SpatialContext reader) {
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
  }

  @Test
  public void testSimpleArgs() throws Exception {
    checkSimpleArgs(new SimpleSpatialContext());
  }

  @Test
  public void testJTSArgs() throws Exception {
    SpatialContext reader = new JtsSpatialContext();
    checkSimpleArgs(reader);

    // now check the complex stuff...
  }
}
