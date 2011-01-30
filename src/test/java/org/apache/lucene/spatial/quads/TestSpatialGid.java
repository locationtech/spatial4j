package org.apache.lucene.spatial.quads;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.lucene.spatial.quads.SpatialGrid;



/**
 */
public class TestSpatialGid extends TestCase
{
  public void testPrintInfo() throws Exception
  {
    SpatialGrid g = new SpatialGrid();
    g.printInfo();

//    BBoxIndexible bbox = new BBoxIndexible( 0,10,0,10 );
//
//    assertEquals( MatchState.COVERS, bbox.test(2, 8, 2, 8) );
//    assertEquals( MatchState.TOUCHES, bbox.test(2, 13, 2, 8) );
//    assertEquals( MatchState.MISS, bbox.test(-1, -.2, 2, 8) );
//    assertEquals( MatchState.MISS, bbox.test(2, 13, 11, 15) );
//
//
//    g = new SpatialGrid( 0, 10, 0, 10, 10 );
//    bbox = new BBoxIndexible( 2.3,7.5,2.5,7.5 );

//    MutableInt bboxLevel = new MutableInt();
//    List<String> boxes = g.read( bbox, bboxLevel );

//    for( String b : boxes ) {
//      System.out.print( "\""+b+"\"," );
//     // System.out.println( g.getRectangle( b ) );
//    }
//    System.out.println( );
//    System.out.println( boxes.size() );
  }
}
