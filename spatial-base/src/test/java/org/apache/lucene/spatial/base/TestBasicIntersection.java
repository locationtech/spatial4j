package org.apache.lucene.spatial.base;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.jts.JtsEnvelope;
import org.apache.lucene.spatial.base.shape.jts.JtsPoint2D;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 */
public class TestBasicIntersection {

  @Test
  public void testIntersection() {
    SpatialPrefixGrid grid = new LinearPrefixGrid(0, 10, 0, 10, 10);

    JtsEnvelope big = new JtsEnvelope(0, 100, 0, 100);
    JtsEnvelope rr0 = new JtsEnvelope(25, 75, 25, 75);
    JtsEnvelope rr1 = new JtsEnvelope(120, 150, 0, 100);
    JtsEnvelope rr2 = new JtsEnvelope(-1, 50, 0, 50);

    assertEquals(IntersectCase.CONTAINS, big.intersect(rr0, grid));
    assertEquals(IntersectCase.WITHIN, rr0.intersect(big, grid));
    assertEquals(IntersectCase.OUTSIDE, big.intersect(rr1, grid));
    assertEquals(IntersectCase.OUTSIDE, rr1.intersect(big, grid));
    assertEquals(IntersectCase.INTERSECTS, rr2.intersect(big, grid));
    assertEquals(IntersectCase.INTERSECTS, big.intersect(rr2, grid));

    GeometryFactory f = new GeometryFactory();
    JtsPoint2D p1 = new JtsPoint2D(f.createPoint(new Coordinate(1000, 20)));
    JtsPoint2D p2 = new JtsPoint2D(f.createPoint(new Coordinate(50, 50)));
    assertEquals(IntersectCase.OUTSIDE, p1.intersect(big, grid));
    assertEquals(IntersectCase.WITHIN, p2.intersect(big, grid));
  }
}
