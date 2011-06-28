package org.apache.solr.spatial.demo.servlet;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.prefix.QuadPrefixGrid;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.solr.spatial.demo.KMLHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;


public class GridInfoServlet extends HttpServlet
{
  JtsSpatialContext ctx = new JtsSpatialContext();

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
    String name = req.getParameter( "name" );
    Shape shape = null;
    String country = req.getParameter( "country" );
    if( country != null && country.length() == 2 ) {
      InputStream in = getClass().getClassLoader().getResourceAsStream("countries-poly.txt");

      SampleDataReader reader = new SampleDataReader( in );
      while( reader.hasNext() ) {
        SampleData data = reader.next();
        if( country.equalsIgnoreCase( data.id ) ) {
          name = data.name;
          shape = ctx.readShape( data.shape );
          break;
        }
      }

      if( shape == null ) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to find: "+country );
        return;
      }
    }
    int depth = getIntParam( req, "depth", 16 );
    SpatialContext ctx = new JtsSpatialContext();
    QuadPrefixGrid grid = new QuadPrefixGrid( ctx, depth );
    int resolution = getIntParam(req, "resolution", 5);

    // If they don't set a country, then use the input
    if( shape == null ) {
      String geo = req.getParameter( "geo" );
      if( geo == null ) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter: 'geo'" );
        return;
      }
      try {
        shape = ctx.readShape( geo );
      }
      catch( Exception ex ) {
        ex.printStackTrace();
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing geo :: "+ex );
      }
    }

    List<String> info = SpatialPrefixGrid.cellsToTokenStrings(grid.getCells(shape, resolution, false));
    String format = req.getParameter( "format" );
    if( "kml".equals( format ) ) {
      if( name == null || name.length() < 2 ) {
        name = "KML - "+new Date( System.currentTimeMillis() );
      }
      Kml kml = KMLHelper.toKML( name, grid, info );

      res.setHeader("Content-Disposition","attachment; filename=\"" + name + "\";");
      res.setContentType( "application/vnd.google-earth.kml+xml" );
      kml.marshal( res.getOutputStream() );
      return;
    }

    res.setContentType( "text/plain" );
    PrintStream out = new PrintStream( res.getOutputStream() );
    out.println( info.toString() );
  }
}
