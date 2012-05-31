package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import org.junit.Test;

public class JtsPolygonTest extends AbstractTestShapes {

  private final String POLY_STR = "Polygon((-10 30, -40 40, -10 -20, 40 20, 0 0, -10 30))";
  private JtsGeometry POLY_SHAPE;
  private final int DL_SHIFT = 180;//since POLY_SHAPE contains 0 0, I know a shift of 180 will make it cross the DL.
  private JtsGeometry POLY_SHAPE_DL;//POLY_SHAPE shifted by DL_SHIFT to cross the dateline

  //TODO support poly crossing the dateline
  private final boolean TEST_DL_POLY = false;
  //TODO poly.relate(other) doesn't work when other crosses the dateline
  private final boolean TEST_DL_OTHER = false;

  public JtsPolygonTest() {
    super(JtsSpatialContext.GEO_KM);
    POLY_SHAPE = (JtsGeometry) ctx.readShape(POLY_STR);

    if (TEST_DL_POLY && ctx.isGeo()) {
      Geometry pGeom = POLY_SHAPE.getGeom();
      assertTrue(pGeom.isValid());
      //shift 180 to the right
      pGeom = (Geometry) pGeom.clone();
      pGeom.apply(new CoordinateFilter() {
        @Override
        public void filter(Coordinate coord) {
          coord.x = ctx.normX(coord.x+DL_SHIFT);
        }
      });
      pGeom.geometryChanged();
      assertFalse(pGeom.isValid());
      POLY_SHAPE_DL = (JtsGeometry) ctx.readShape(pGeom.toText());
      //assertTrue(POLY_SHAPE_DL.getBoundingBox().getCrossesDateLine());//TODO this is better than 360
      assertEquals(360.0,POLY_SHAPE_DL.getBoundingBox().getWidth(),0.0);//good enough for now
    }
  }

  @Test
  @Repeat(iterations = 100)
  public void testPointAndRectIntersect() {
    Rectangle r = null;
    do{
      r = randomRectangle(2);
    } while(!TEST_DL_OTHER && r.getCrossesDateLine());

    assertJtsConsistentRelate(r);
    assertJtsConsistentRelate(r.getCenter());
  }

  @Test
  public void testRegression() {
    assertJtsConsistentRelate(new PointImpl(-10, 4));//PointImpl not JtsPoint, and CONTAINS
    assertJtsConsistentRelate(new PointImpl(-15, -10));//point on boundary
  }

  private void assertJtsConsistentRelate(Shape shape) {
    IntersectionMatrix expectedM = POLY_SHAPE.getGeom().relate(((JtsSpatialContext) ctx).getGeometryFrom(shape));
    SpatialRelation expectedSR = JtsGeometry.intersectionMatrixToSpatialRelation(expectedM);
    //JTS considers a point on a boundary INTERSECTS, not CONTAINS
    if (expectedSR == SpatialRelation.INTERSECTS && shape instanceof Point)
      expectedSR = SpatialRelation.CONTAINS;
    assertRelation(null, expectedSR, POLY_SHAPE, shape);

    if (TEST_DL_POLY && ctx.isGeo()) {
      //shift shape, set to shape2
      Shape shape2;
      if (shape instanceof Rectangle) {
        Rectangle r = (Rectangle) shape;
        shape2 = ctx.makeRect(r.getMinX()+DL_SHIFT,r.getMaxX()+DL_SHIFT,r.getMinY(),r.getMaxY());
        if (!TEST_DL_OTHER && shape2.getBoundingBox().getCrossesDateLine())
          return;
      } else if (shape instanceof Point) {
        Point p = (Point) shape;
        shape2 = ctx.makePoint(p.getX()+DL_SHIFT,p.getY());
      } else {
        throw new RuntimeException(""+shape);
      }

      assertRelation(null, expectedSR, POLY_SHAPE_DL, shape2);
    }
  }

}
