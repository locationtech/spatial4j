package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import org.junit.Test;

public class JtsPolygonTest extends AbstractTestShapes {

  private final String POLY_STR = "Polygon((-10 30, -40 40, -10 -20, 40 20, 0 0, -10 30))";
  private JtsGeometry POLY_SHAPE;

  public JtsPolygonTest() {
    super(JtsSpatialContext.GEO_KM);
    POLY_SHAPE = (JtsGeometry) ctx.readShape(POLY_STR);
  }

  @Test
  @Repeat(iterations = 100)
  public void testPointAndRectIntersect() {
    Rectangle r = null;
    do{
      r = randomRectangle(2);
    } while(r.getCrossesDateLine());//TODO we don't want one that crosses the dateline; since doesn't work yet

    assertRelate(POLY_SHAPE, r);
    assertRelate(POLY_SHAPE, r.getCenter());
  }

  @Test
  public void testRegression() {
    assertRelate(POLY_SHAPE,new PointImpl(-10,4));//PointImpl not JtsPoint, and CONTAINS
    assertRelate(POLY_SHAPE,new PointImpl(-15,-10));//point on boundary
  }

  private void assertRelate(JtsGeometry s4jJtsGeom, Shape shape) {
    IntersectionMatrix expectedM = s4jJtsGeom.geom.relate(((JtsSpatialContext) ctx).getGeometryFrom(shape));
    SpatialRelation expectedSR = jtsIntersectionMatrixToS4jSpatialRelation(expectedM);
    //JTS considers a point on a boundary INTERSECTS, not CONTAINS
    if (expectedSR == SpatialRelation.INTERSECTS && shape instanceof Point)
      expectedSR = SpatialRelation.CONTAINS;
    assertRelation(null,expectedSR, s4jJtsGeom, shape);
  }

  private SpatialRelation jtsIntersectionMatrixToS4jSpatialRelation(IntersectionMatrix matrix) {
    SpatialRelation spatialRelation;
    if (matrix.isContains())
      spatialRelation = SpatialRelation.CONTAINS;
    else if (matrix.isCoveredBy())
      spatialRelation = SpatialRelation.WITHIN;
    else if (matrix.isDisjoint())
      spatialRelation = SpatialRelation.DISJOINT;
    else
      spatialRelation = SpatialRelation.INTERSECTS;
    return spatialRelation;
  }
}
