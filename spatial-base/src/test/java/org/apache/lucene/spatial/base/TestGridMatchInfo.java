package org.apache.lucene.spatial.base;

import java.util.List;

import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.junit.Test;


/**
 */
public class TestGridMatchInfo {

  @Test
  public void testMatchInfo() {
    // Check Validatio
    LinearPrefixGrid grid = new LinearPrefixGrid(0, 10, 0, 10, 2);
    grid.setMinResolution(1);
    grid.setResolution(1);

//    GeometricShapeFactory gsf = new GeometricShapeFactory();
//    gsf.setCentre( new com.vividsolutions.jts.geom.Coordinate( 5,5 ) );
//    gsf.setSize( 9.5 );
//    Shape shape = new JtsGeometry( gsf.createCircle() );

    Shape shape = new Rectangle(0, 6, 5, 10);

    shape = new Point2D(3, 3);

    List<String> m = grid.readCells(shape);
    System.out.println(m);

    for (CharSequence s : m) {
      System.out.println(s);
    }


//    // query should intersect everything one level down
//    ArrayList<String> descr = new ArrayList<String>();
//    descr.add( "AAA*" );
//    descr.add( "AABC*" );
//    System.out.println( descr );
  }
}
