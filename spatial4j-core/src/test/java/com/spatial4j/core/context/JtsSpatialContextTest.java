package com.spatial4j.core.context;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Note that {@link SpatialContextTest} already tests JTS but we want to exercise some JTS specifics here.
 */
public class JtsSpatialContextTest extends RandomizedTest {

  SpatialContext ctx = JtsSpatialContext.GEO_KM;

  @Test
  public void wktGeoPt() throws IOException {
    Shape s = ctx.readShape("Point(-160 30)");
    assertEquals(ctx.makePoint(-160,30),s);
  }

  @Test
  public void wktGeoRect() throws IOException {
    //REMEMBER: Polygon WKT's outer ring is counter-clockwise order. If you accidentally give the other direction,
    // JtsSpatialContext will give the wrong result for a rectangle crossing the dateline.

    // In these two tests, we give the same set of points, one that does not cross the dateline, and the 2nd does. The
    // order is counter-clockwise in both cases as it should be.

    Shape sNoDL = ctx.readShape("Polygon((-170 30, -170 15,  160 15,  160 30, -170 30))");
    Rectangle expectedNoDL = ctx.makeRect(-170, 160, 15, 30);
    assertTrue(!expectedNoDL.getCrossesDateLine());
    assertEquals(expectedNoDL,sNoDL);

    Shape sYesDL = ctx.readShape("Polygon(( 160 30,  160 15, -170 15, -170 30,  160 30))");
    Rectangle expectedYesDL = ctx.makeRect(160, -170, 15, 30);
    assertTrue(expectedYesDL.getCrossesDateLine());
    assertEquals(expectedYesDL,sYesDL);

  }

  @Test
  public void readRussiaWkt() throws IOException {
    // Russia is a multiPolygon and it crosses the dateline
    String wktStr = null;
    InputStream is = getClass().getResourceAsStream("/russia.wkt.txt");
    assertNotNull(is);
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      wktStr = br.readLine();
    } finally {
      is.close();
    }
    //Error (! valid): Nested shells at or near point (137.2213092954354, 54.77371813483103, NaN)

    ctx.readShape(wktStr);
  }

}
