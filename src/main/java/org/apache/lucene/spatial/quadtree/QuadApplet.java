package org.apache.lucene.spatial.quadtree;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JApplet;

import org.apache.lucene.spatial.quadtree.shape.BBoxIndexible;
import org.apache.lucene.spatial.quadtree.shape.JTSIndexible;
import org.apache.lucene.spatial.quadtree.shape.PointRadiusIndexible;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class QuadApplet extends JApplet
{
  private SpatialGrid grid;
  private List<String> vals;

  public QuadApplet() throws Exception
  {
    grid = new SpatialGrid( 0, 10, 0, 10, 10 );
    grid.resolution = 5; // how far past the best fit to go

    QuadIndexable shape = null;

    shape = new BBoxIndexible( 0.01, 10, 0.01, 10 ); //.1, .4, .1, .4 ); //2.3,7.5,2.5,7.5 );

    shape = new PointRadiusIndexible( 5,5, 5 );

    shape = new JTSIndexible( "MULTIPOINT((3.5 5.6), (4.8 9.5))" );
    shape = new JTSIndexible( "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))" );
    shape = new JTSIndexible( "POINT(6 8)" ); // very small display!
    shape = new JTSIndexible( "POINT(3.5 5.6)" );

    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre( new Coordinate( 5,5 ) );
    gsf.setSize( 10 );
    shape = new JTSIndexible( gsf.createCircle() );


   // shape = new JTSIndexible( "LINESTRING(2 2, 5 7, 8 1 )" );

    vals = grid.read( shape, null );
    System.out.println( "size: "+vals.size() );
    System.out.println( vals );
  }


  @Override
  public void paint(Graphics g) {

    int square = Math.min( getWidth(), getHeight() );

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

    double gridW = grid.xmax - grid.xmin;
    double gridH = grid.ymax - grid.ymin;

    for( String c : vals ) {
      BBoxIndexible r = grid.getRectangle( c );
      double px = r.getMinX() / gridW;
      double py = r.getMinY() / gridH;
      double pw = r.getWidth()  / gridW;
      double ph = r.getHeight() / gridH;

      if( c.endsWith( "*" ) ) {
        g.setColor( Color.GREEN );
        g.fillRect( (int)(square*px), (int)(square*py), (int)(square*pw), (int)(square*ph) );

        g.setColor( Color.BLACK );
      }
      else if( c.endsWith( "-" ) ) {
        g.setColor( Color.LIGHT_GRAY );
      }
      else {
        g.setColor( Color.GRAY );
      }
      g.drawRect( (int)(square*px), (int)(square*py), (int)(square*pw), (int)(square*ph) );
    }


  }
}