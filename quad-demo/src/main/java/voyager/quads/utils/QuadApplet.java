package voyager.quads.utils;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JApplet;

import voyager.quads.LevelMatchInfo;
import voyager.quads.MatchInfo;
import voyager.quads.SpatialGrid;
import voyager.quads.geometry.GeometryShape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class QuadApplet extends JApplet
{
  private SpatialGrid grid;
  private MatchInfo vals;

  public QuadApplet() throws Exception
  {
    grid = new SpatialGrid( 0, 10, 0, 10, 10 );
    //grid = new SpatialGrid( -180, 180, -90, 90, 16 );
    grid.resolution = 4; // how far past the best fit to go

    Geometry shape = null;

//    shape = new PointRadiusIndexible( 5,5, 5 );
//
//    shape = JtsGeom.parseGeometry( "MULTIPOINT((3.5 5.6), (4.8 9.5))" );
//    shape = JtsGeom.parseGeometry( "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))" );
//    shape = JtsGeom.parseGeometry( "LINESTRING(3 4,5 7,8 2)" );


//    shape = new JTSIndexible( "POINT(6 8)" ); // very small display!
//    shape = new JTSIndexible( "POINT(3.5 5.6)" );

    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre( new Coordinate( 4,2 ) );
    gsf.setSize( 4 );
    shape = gsf.createCircle();


    vals = grid.read( new GeometryShape(shape) );
    vals.printInfo();
  }

  int square;
  double gridW;
  double gridH;


  @Override
  public void paint(Graphics g) {

    square = Math.min( getWidth(), getHeight() );

    Graphics g2d = g;
    g2d.clearRect(0,0,getWidth(),getHeight());

    g.setColor( Color.WHITE );
    g.fillRect(0, 0, getWidth(), getHeight() );
    g.setColor( Color.DARK_GRAY );
    g.drawRect(0, 0, square-1, square-1 );

//    // Circle debugging
//    int mid = square/2;
//    g.setColor( Color.BLUE );
//    g.drawArc(0, 0, square, square, 0, 360);
//    g.drawLine(0, mid, mid, 0);
//    g.drawLine(mid, 0, square, mid);
//    g.drawLine(0, mid, mid, square);
//    g.drawLine(mid, square, square, mid);

    gridW = grid.xmax - grid.xmin;
    gridH = grid.ymax - grid.ymin;

    for( LevelMatchInfo level : vals.levels ) {

      for( String c : level.intersects ) {
        draw( g, c, Color.GRAY, null );
      }
      for( String c : level.depth ) {
        draw( g, c, Color.BLACK, Color.PINK );
      }
      for( String c : level.covers ) {
        draw( g, c, Color.BLACK, Color.GREEN );
      }
    }
  }

  private void draw( Graphics g, String c, Color line, Color fill )
  {
    Envelope r = grid.getRectangle( c );
    double px = r.getMinX() / gridW;
    double py = r.getMinY() / gridH;
    double pw = r.getWidth()  / gridW;
    double ph = r.getHeight() / gridH;

    if( fill != null ) {
      g.setColor( fill );
      g.fillRect( (int)(square*px), (int)(square*py), (int)(square*pw), (int)(square*ph) );
    }
    if( line != null ) {
      g.setColor( line );
      g.drawRect( (int)(square*px), (int)(square*py), (int)(square*pw), (int)(square*ph) );
    }
  }
}