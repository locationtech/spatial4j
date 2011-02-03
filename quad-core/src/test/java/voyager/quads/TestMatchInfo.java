package voyager.quads;

import java.util.ArrayList;

import junit.framework.TestCase;
import voyager.quads.geometry.GeometryShape;

import com.vividsolutions.jts.util.GeometricShapeFactory;



/**
 */
public class TestMatchInfo extends TestCase
{
  public void testMatchInfo() throws Exception
  {
    // Check Validatio
    SpatialGrid grid = new SpatialGrid( 0, 10, 0, 10, 10 );
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre( new com.vividsolutions.jts.geom.Coordinate( 4,2 ) );
    gsf.setSize( 4 );
    MatchInfo m = grid.read( new GeometryShape( gsf.createCircle() ) );
    m.validate(); // check that the lengths are what we expect

    // should create stubs for 0-5
    m = new MatchInfo();
    LevelMatchInfo level = m.getLevelInfo(4, true);
    level.covers.add( "AAAAA" );
    assertEquals( 5, m.levels.size() );
    // make sure it has the right lengths
    m.validate();

    // query should intersect everything one level down
    ArrayList<String> descr = new ArrayList<String>();
    descr.add( "AAA*" );
    descr.add( "AABC*" );
    MatchInfo q = MatchInfo.getMostlyWithinQueryTokens( descr );
    q.printInfo( System.out );
    q.validate();
  }
}
