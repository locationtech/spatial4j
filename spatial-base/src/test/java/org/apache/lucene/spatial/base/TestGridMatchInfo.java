package org.apache.lucene.spatial.base;

import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.grid.LinearSpatialGrid;
import org.apache.lucene.spatial.base.simple.Point2D;
import org.apache.lucene.spatial.base.simple.Rectangle;



/**
 */
public class TestGridMatchInfo extends TestCase
{
  public void testMatchInfo() throws Exception
  {
    // Check Validatio
    LinearSpatialGrid grid = new LinearSpatialGrid( 0, 10, 0, 10, 2 );
    grid.resolution = 1;
    grid.minResolution = 1;

//    GeometricShapeFactory gsf = new GeometricShapeFactory();
//    gsf.setCentre( new com.vividsolutions.jts.geom.Coordinate( 5,5 ) );
//    gsf.setSize( 9.5 );
//    Shape shape = new JtsGeometry( gsf.createCircle() );

    Shape shape = new Rectangle( 0, 6, 5, 10 );

    shape = new Point2D( 3, 3 );

    List<CharSequence> m = grid.readCells( shape );
    System.out.println( m );

    for( CharSequence s : m ) {
      System.out.println( s );
    }


//    // query should intersect everything one level down
//    ArrayList<String> descr = new ArrayList<String>();
//    descr.add( "AAA*" );
//    descr.add( "AABC*" );
//    System.out.println( descr );
  }
}
