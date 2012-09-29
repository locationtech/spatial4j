package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.io.IOException;

/**
 * Note that {@link ShapeCodecTest} already tests JTS but we want to exercise some JTS specifics here.
 */
public class JtsShapeCodecTest extends RandomizedTest {

  JtsSpatialContext ctx = JtsSpatialContext.GEO;
  ShapeCodec<JtsSpatialContext> codec = new JtsShapeCodec(ctx);

  @Test
  public void wktGeoPt() throws IOException {
    Shape s = codec.readShape("Point(-160 30)");
    assertEquals(ctx.makePoint(-160,30),s);
  }

  @Test
  public void wktGeoRect() throws IOException {
    //REMEMBER: Polygon WKT's outer ring is counter-clockwise order. If you accidentally give the other direction,
    // JtsSpatialContext will give the wrong result for a rectangle crossing the dateline.

    // In these two tests, we give the same set of points, one that does not cross the dateline, and the 2nd does. The
    // order is counter-clockwise in both cases as it should be.

    Shape sNoDL = codec.readShape("Polygon((-170 30, -170 15,  160 15,  160 30, -170 30))");
    Rectangle expectedNoDL = ctx.makeRectangle(-170, 160, 15, 30);
    assertTrue(!expectedNoDL.getCrossesDateLine());
    assertEquals(expectedNoDL,sNoDL);

    Shape sYesDL = codec.readShape("Polygon(( 160 30,  160 15, -170 15, -170 30,  160 30))");
    Rectangle expectedYesDL = ctx.makeRectangle(160, -170, 15, 30);
    assertTrue(expectedYesDL.getCrossesDateLine());
    assertEquals(expectedYesDL,sYesDL);
  }

}
