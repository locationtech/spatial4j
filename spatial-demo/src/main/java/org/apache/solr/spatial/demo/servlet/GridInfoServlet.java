package org.apache.solr.spatial.demo.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.jts.JtsGeometry;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.solr.spatial.demo.utils.KMLHelper;
import org.apache.solr.spatial.demo.utils.countries.CountryInfo;
import org.apache.solr.spatial.demo.utils.countries.CountryReader;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.micromata.opengis.kml.v_2_2_0.Kml;


public class GridInfoServlet extends HttpServlet
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
    String name = req.getParameter( "name" );
    Shape shape = null;
    String country = req.getParameter( "country" );
    if( country != null && country.length() == 2 ) {

      File file = new File( "../data/countries/cntry06.shp" );
      System.out.println( "reading: "+file.getAbsolutePath() );

      ShapeReader reader = new ShapeReader( file );
      FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
      while( iter.hasNext() ) {
        CountryInfo info = new CountryReader().read( iter.next() );
        if( country.equalsIgnoreCase( info.id ) ) {
          name = info.name;
          shape = new JtsGeometry( info.geometry );
          break;
        }
      }

      if( shape == null ) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to find: "+country );
        return;
      }
    }
    int depth = getIntParam( req, "depth", 16 );
    ShapeIO reader = new JtsShapeIO( DistanceUnits.KILOMETERS );
    LinearPrefixGrid grid = new LinearPrefixGrid( -180, 180, -90-180, 90, depth ); // make it like WGS84
    grid.setResolution(getIntParam(req, "resolution", 4));

    // If they don't set a country, then use the input
    if( shape == null ) {
      String geo = req.getParameter( "geo" );
      if( geo == null ) {
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter: 'geo'" );
        return;
      }
      try {
        shape = reader.readShape( geo );
      }
      catch( Exception ex ) {
        ex.printStackTrace();
        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing geo :: "+ex );
      }
    }

    List<CharSequence> info = grid.readCells( shape );
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
    return;
  }
}