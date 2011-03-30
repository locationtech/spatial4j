package org.apache.solr.spatial.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.lucene.spatial.base.io.geonames.Geoname;
import org.apache.lucene.spatial.base.io.geonames.GeonamesReader;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.countries.BasicInfo;
import org.apache.solr.spatial.demo.utils.countries.BasicReader;
import org.apache.solr.spatial.demo.utils.countries.CountryReader;
import org.apache.solr.spatial.demo.utils.geoeye.GeoeyeReader;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;


public class SampleDataLoader
{

  public static void writeCountriesToCSV( File shp, BasicReader<?> rrr, File fout ) throws Exception
  {
    PrintWriter out = new PrintWriter( new OutputStreamWriter(
        new FileOutputStream(fout), "UTF8") );

    out.print( "#name" );
    out.print( '\t' );
    out.print( "fips" );
    out.print( '\t' );
    out.print( "population2005" );
    out.print( '\t' );
    out.print( "geo" );
    out.print( '\t' );
    out.println();
    out.flush();

    ShapeReader reader = new ShapeReader( shp );
    //reader.describe( System.out );

    int total = reader.getCount();
    int count = 0;
    FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
    while( iter.hasNext() ) {
      BasicInfo c = rrr.read( iter.next() );

      int maxlen = 27500;
      String geo = c.geometry.toText();
      if( geo.length() > maxlen ) {
        Geometry ggg = c.geometry;
        System.out.println( "TODO, simplify: "+c.name );

        long last = geo.length();
        Envelope env = ggg.getEnvelopeInternal();
        double mins = Math.min(env.getWidth(), env.getHeight());
        double div = 1000;
        while (true) {
          double tolerance = mins / div;
          System.out.println( c.name + " :: Simplifying long geometry: WKT.length=" + geo.length() + " tolerance=" + tolerance);
          Geometry simple = TopologyPreservingSimplifier.simplify(ggg, tolerance);
          geo = simple.toText();
          if (geo.length() < maxlen) {
            break;
          }
          if (geo.length() == last) {
            System.out.println( c.name + " :: Can not simplify geometry smaller then max. " + last);
            break;
          }
          last = geo.length();
          div *= .70;
        }
      }

      out.print( c.name );
      out.print( '\t' );
      out.print( c.fips );
      out.print( '\t' );
      out.print( c.population2005 );
      out.print( '\t' );
      out.print( geo );
      out.print( '\t' );
      out.println();
      out.flush();

    //  System.out.println( (++count)+"/"+total + " :: " +c.name );
    }

    out.flush();
    out.close();
  }


  public static void load( SolrServer solr ) throws Exception
  {
    File file = null;
    file = new File( "../data/ikonos_2011/ikonos_2011.shp" );
    if( false && file.exists() ) {
      GeoeyeReader.indexItems( solr, file );
      solr.commit( true, true );
    }

    file = new File( "../data/countries/cntry06.shp" );
    if( false && file.exists() ) {
      new CountryReader().index( solr, file );
      solr.commit( true, true );
    }

    // Geonames
    file = new File( "../data/geonames/US.txt" );
    if( file.exists() ) {
      GeonamesReader reader = new GeonamesReader( file );
      while( reader.hasNext() ) {
        Geoname name = reader.next();
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField( "id", name.id+"" );
        doc.setField( "name", name.name+"" );
        doc.setField( "geo", name.longitude + " " + name.latitude );
        doc.setField( "source", "geonames_US" );
        solr.add( doc );

        if( (reader.getCount() % 1000) == 0 ) {
          System.out.println( "geonames: "+reader.getCount() + " :: " + name.name );
        }
      }
      solr.commit( true, true );
    }
  }

  public static void main( String[] args ) throws Exception
  {
    File file = new File( "../data/countries/cntry06.shp" ); //cntry06.shp" );
    if( true ) {
      File fout = new File( "c:/temp/country.txt" );
      writeCountriesToCSV(file, new CountryReader(), fout );
      System.out.println( "done." );
      return;
    }

    SolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
  //  SolrServer solr = new CommonsHttpSolrServer( "http://localhost:8080/solr" );

    load( solr );
    System.out.println( "done." );
  }
}
