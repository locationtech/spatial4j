package org.locationtech.spatial4j.io;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import org.junit.Test;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.ShapeCollection;

public class WKTWriterTest {

  private SpatialContext ctx;

  protected WKTWriterTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public WKTWriterTest() {
    this(SpatialContext.GEO);
  }

  @Test
  public void testToStringOnEmptyPoint() throws Exception {
    ShapeWriter writer = ctx.getFormats().getWktWriter();
    Point emptyPoint = ctx.makePoint(Double.NaN, Double.NaN);

    assertEquals("POINT EMPTY", writer.toString(emptyPoint));
  }

  @Test
  public void testToStringOnEmptyShapeCollection() throws Exception {
    ShapeWriter writer = ctx.getFormats().getWktWriter();
    ShapeCollection<Point> emptyCollection = ctx.makeCollection(new ArrayList<>());

    assertEquals("GEOMETRYCOLLECTION EMPTY", writer.toString(emptyCollection));
  }
}
