package org.apache.solr.spatial.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.lucene.spatial.base.io.geonames.Geoname;
import org.apache.lucene.spatial.base.io.geonames.GeonamesReader;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.io.sample.SampleDataWriter;
import org.apache.lucene.spatial.base.shape.jts.JtsEnvelope;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.countries.BasicInfo;
import org.apache.solr.spatial.demo.utils.countries.BasicReader;
import org.apache.solr.spatial.demo.utils.countries.CountryReader;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;


public class SampleDataLoader
{

  public static void writeCountriesToCSV( File shp, BasicReader<?> rrr, File fout, boolean bbox ) throws Exception
  {
    PrintWriter out = new PrintWriter( new OutputStreamWriter(
        new FileOutputStream(fout), "UTF8") );

    out.print( "#id" );
    out.print( '\t' );
    out.print( "name" );
    out.print( '\t' );
    out.print( "shape" );
    out.print( '\t' );
    out.println();
    out.flush();

    JtsShapeIO shapeIO = new JtsShapeIO();
    ShapeReader reader = new ShapeReader( shp );
    reader.describe( System.out );

    int total = reader.getCount();
    int count = 0;
    FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
    while( iter.hasNext() ) {
      BasicInfo c = rrr.read( iter.next() );
      String geo = null;
      if( bbox ) {
        geo = shapeIO.toString( new JtsEnvelope(c.geometry.getEnvelopeInternal()) );
      }
      else {
        int maxlen = 27500;
        geo = c.geometry.toText();
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
      }

      out.print( c.id );
      out.print( '\t' );
      out.print( c.name );
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

  private static void indexSampleData( SolrServer solr, File f ) throws Exception {
    System.out.println( "indexing: "+f.getAbsolutePath() );
    SampleDataReader reader = new SampleDataReader( f );
    while( reader.hasNext() ) {
      SampleData data = reader.next();
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", data.id );
      doc.setField( "name", data.name );
      doc.setField( "geo", data.shape );
      doc.setField( "source", f.getName() );
      solr.add( doc );
    }
    solr.commit( true, true );
  }


  public static void load( SolrServer solr ) throws Exception
  {
    File file = null;

    //file = new File(SampleDataLoader.class.getClassLoader().getResource("us-states.txt").getFile());

    File basedir = new File( "../spatial-data/src/main/resources" );
    File[] data = new File[] {
        new File(basedir, "world-cities-points.txt" ),
        new File(basedir, "countries-poly.txt" ),
        new File(basedir, "countries-bbox.txt" ),
        new File(basedir, "states-poly.txt" ),
        new File(basedir, "states-bbox.txt" ),
    };

    for( File f : data ) {
      if( f.exists() ) {
        indexSampleData( solr, f );
      }
    }

    file = new File( basedir, "countries.txt" );
    if( file.exists() ) {
      indexSampleData( solr, file );
    }

    // Geonames
    file = new File( "../data/geonames/US.txt" );
    if( false && file.exists() ) {
      GeonamesReader reader = new GeonamesReader( file );
      while( reader.hasNext() ) {
        Geoname name = reader.next();
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField( "id", name.id+"" );
        doc.setField( "name", name.name+"" );
        doc.setField( "geo", name.longitude + " " + name.latitude );
        doc.setField( "source", "geonames-"+file.getName() );
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
//    File basedir = new File( "../spatial-data/src/main/resources" );
//    File file = new File( basedir, "us-states.txt" );
//    if( true ) {
//      SampleDataReader reader = new SampleDataReader( file );
//      while( reader.hasNext() ) {
//        SampleData data = reader.next();
//        System.out.println( data.id );
//      }
//      System.out.println( reader.getCount() );
//      return;
//    }


    File file = new File( "../spatial-data/src/main/resources/geonames/cities15000.txt" ); //states.shp" ); //cntry06.shp" );
    if( true ) {
      File fout = new File( "c:/temp/cities-points.txt" );
      SampleDataWriter out = new SampleDataWriter( fout );
      GeonamesReader reader = new GeonamesReader( file );
      while( reader.hasNext() ) {
        Geoname place = reader.next();
        if( place.population > 50000 ) {
          System.out.println( "INCLUDE: " + place.population + "  : " + place.name );
          out.write( "G"+place.id , place.name, place.longitude, place.latitude );
        }
        else {
          System.out.println( "SKIP: " + place.population + "  : " + place.name );
        }
      }
      out.close();

      System.out.println( "done." );
      return;
    }


    file = new File( "../spatial-data/src/main/resources/countries/cntry06.shp" ); //states.shp" ); //cntry06.shp" );
    if( true ) {
      System.out.println( "bbox:" );
      File fout = new File( "c:/temp/country-bbox.txt" );
      writeCountriesToCSV(file, new CountryReader(), fout, true );

      System.out.println( "poly:" );
      fout = new File( "c:/temp/country-poly.txt" );
      writeCountriesToCSV(file, new CountryReader(), fout, false );
      System.out.println( "done." );
      return;
    }

    SolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
  //  SolrServer solr = new CommonsHttpSolrServer( "http://localhost:8080/solr" );

    load( solr );
    System.out.println( "done." );
  }
}
