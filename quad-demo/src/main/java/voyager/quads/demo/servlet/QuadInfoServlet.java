package voyager.quads.demo.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import voyager.quads.MatchInfo;
import voyager.quads.SpatialGrid;
import voyager.quads.geometry.Shape;
import voyager.quads.utils.KMLHelper;
import de.micromata.opengis.kml.v_2_2_0.Kml;


public class QuadInfoServlet extends HttpServlet
{
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public static int getIntParam( HttpServletRequest req, String p, int defaultValue )
  {
    String v = req.getParameter( p );
    if( v != null && v.length() > 0 ) {
      return Integer.parseInt( v );
    }
    return defaultValue;
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
    int depth = getIntParam( req, "depth", 16 );
    SpatialGrid grid = new SpatialGrid( -180, 180, -90-180, 90, depth ); // make it like WGS84
    grid.resolution = getIntParam( req, "resolution", 4 );

    MatchInfo info = grid.read( shape );
    String format = req.getParameter( "format" );
    if( "kml".equals( format ) ) {
      String name = req.getParameter( "name" );
      if( name == null || name.length() < 2 ) {
        name = "KML - "+new Date( System.currentTimeMillis() );
      }
      boolean showIntersects = "on".equals( req.getParameter( "intersections" ) );
      Kml kml = KMLHelper.toKML( info, name, grid, showIntersects );

      res.setHeader("Content-Disposition","attachment; filename=\"" + name + "\";");
      res.setContentType( "application/vnd.google-earth.kml+xml" );
      kml.marshal( res.getOutputStream() );
      return;
    }

    res.setContentType( "text/plain" );
    PrintStream out = new PrintStream( res.getOutputStream() );
    info.printInfo( out );
    return;
  }
}