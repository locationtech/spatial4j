package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import org.junit.Test;

public class JtsPolygonTest extends AbstractTestShapes {

  private final String POLY_STR = "Polygon((-10 30, -40 40, -10 -20, 40 20, 0 0, -10 30))";

  public JtsPolygonTest() {
    super(JtsSpatialContext.GEO_KM);
  }

  @Test
  @Repeat(iterations = 100)
  public void testPointAndRectIntersect() {
    JtsGeometry s4jJtsGeom = (JtsGeometry) ctx.readShape(POLY_STR);

    Rectangle r = null;
    do{
      r = randomRectangle(2);
    } while(r.getCrossesDateLine());//TODO we don't want one that crosses the dateline; since doesn't work yet

    IntersectionMatrix expected = s4jJtsGeom.geom.relate(((JtsSpatialContext) ctx).getGeometryFrom(r));
    SpatialRelation expectedSR = jtsIntersectionMatrixToS4jSpatialRelation(expected);

    assertRelation(null,expectedSR, s4jJtsGeom, r);
  }

  private SpatialRelation jtsIntersectionMatrixToS4jSpatialRelation(IntersectionMatrix expected) {
    SpatialRelation expectedSR;
    if (expected.isContains())
      expectedSR = SpatialRelation.CONTAINS;
    else if (expected.isCoveredBy())
      expectedSR = SpatialRelation.WITHIN;
    else if (expected.isDisjoint())
      expectedSR = SpatialRelation.DISJOINT;
    else
      expectedSR = SpatialRelation.INTERSECTS;
    return expectedSR;
  }
}
