package com.spatial4j.demo.servlet;

import com.spatial4j.demo.app.WicketApplication;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.apache.commons.io.IOUtils;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.io.sample.SampleData;
import com.spatial4j.core.io.sample.SampleDataReader;
import com.spatial4j.core.shape.Shape;

import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;

import com.spatial4j.demo.KMLHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;


public class GridInfoServlet extends HttpServlet
{
  JtsSpatialContext ctx = JtsSpatialContext.GEO_KM;

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

  public static double getDoubleParam( HttpServletRequest req, String p, double defaultValue )
  {
    String v = req.getParameter( p );
    if( v != null && v.length() > 0 ) {
      return Double.parseDouble( v );
    }
    return defaultValue;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
  {
    String name = req.getParameter( "name" );
    Shape shape = null;
    String country = req.getParameter( "country" );
    if( country != null && country.length() == 3 ) {
      InputStream in = WicketApplication.getStreamFromDataResource("countries-poly.txt");
      try {
        SampleDataReader reader = new SampleDataReader( in );
        while( reader.hasNext() ) {
          SampleData data = reader.next();
          if( country.equalsIgnoreCase( data.id ) ) {
            name = data.name;
            shape = ctx.readShape( data.shape );
            break;
          }
        }
      } finally {
        IOUtils.closeQuietly(in);
      }

      if( shape == null ) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to find: "+country );
        return;
      }
    }
    int depth = getIntParam( req, "depth", 16 );

    String gridtype = req.getParameter("gridType");
    
    SpatialPrefixTree grid;
    if ("geohash".equals(gridtype)) {
      grid = new GeohashPrefixTree(ctx, depth);
    } else if ("quad".equals(gridtype)) {
      grid = new QuadPrefixTree( ctx, depth );
    } else {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "unknown grid type: "+gridtype );
      return;
    }
   
    double distErrPct = getDoubleParam(req, "distErrPct", SpatialArgs.DEFAULT_DIST_PRECISION);

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
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing geo: "+ex );
      }
    }
    int detailLevel = grid.getMaxLevelForPrecision(shape,distErrPct);
    log("Using detail level "+detailLevel);
    List<String> info = SpatialPrefixTree.nodesToTokenStrings(grid.getNodes(shape, detailLevel, false));
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
    out.println("Number of tokens: "+info.size());
    out.println( info.toString() );
  }
}
