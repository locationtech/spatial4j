package voyager.quads.demo.servlet;

import java.io.IOException;
import java.sql.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import voyager.quads.MatchInfo;
import voyager.quads.SpatialGrid;
import voyager.quads.geometry.Shape;


public class QuadInfoServlet extends HttpServlet
{
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
  {
    String geo = req.getParameter( "geo" );
    if( geo == null ) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter: 'geo'" );
      return;
    }
    Shape shape = null;
    try {
      shape = Shape.parse( geo );
    }
    catch( Exception ex ) {
      ex.printStackTrace();
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing geo :: "+ex );
    }
    SpatialGrid grid = new SpatialGrid( -180, 180, -90-180, 90, 16 ); // make it like WGS84

    MatchInfo info = grid.read( shape );

    String format = req.getParameter( "format" );
    if( "kml".equals( format ) ) {
      String name = req.getParameter( "name" );
      if( name == null ) {
        name = "KML - "+new Date( System.currentTimeMillis() );
      }

      return;
    }


  }
}