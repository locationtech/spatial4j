package org.apache.lucene.spatial.quads;

import junit.framework.TestCase;

import org.apache.lucene.spatial.core.IntersectCase;
import org.apache.lucene.spatial.grid.SpatialGrid;
import org.apache.lucene.spatial.grid.jts.JtsEnvelope;
import org.apache.lucene.spatial.grid.jts.JtsPoint2D;
import org.apache.lucene.spatial.grid.linear.LinearSpatialGrid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;



/**
 */
public class TestBasicIntersection extends TestCase
{
  public void testIntersection() throws Exception
  {
    SpatialGrid grid = new LinearSpatialGrid( 0, 10, 0, 10, 10 );

    JtsEnvelope big = new JtsEnvelope(   0, 100,  0, 100 );
    JtsEnvelope rr0 = new JtsEnvelope(  25,  75, 25,  75 );
    JtsEnvelope rr1 = new JtsEnvelope( 120, 150,  0, 100 );
    JtsEnvelope rr2 = new JtsEnvelope(  -1,  50,  0,  50 );

    assertEquals( IntersectCase.CONTAINS, big.intersect( rr0, grid ) );
    assertEquals( IntersectCase.WITHIN, rr0.intersect( big, grid ) );
    assertEquals( IntersectCase.OUTSIDE, big.intersect( rr1, grid ) );
    assertEquals( IntersectCase.OUTSIDE, rr1.intersect( big, grid ) );
    assertEquals( IntersectCase.INTERSECTS, rr2.intersect( big, grid ) );
    assertEquals( IntersectCase.INTERSECTS, big.intersect( rr2, grid ) );

    GeometryFactory f = new GeometryFactory();
    JtsPoint2D p1 = new JtsPoint2D( f.createPoint( new Coordinate( 1000, 20 ) ) );
    JtsPoint2D p2 = new JtsPoint2D( f.createPoint( new Coordinate( 50, 50 ) ) );
    assertEquals( IntersectCase.OUTSIDE, p1.intersect( big, grid ) );
    assertEquals( IntersectCase.WITHIN, p2.intersect( big, grid ) );
  }
}
