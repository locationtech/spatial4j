package org.apache.solr.spatial.demo;

import java.io.File;

import org.apache.lucene.spatial.base.io.geonames.Geoname;
import org.apache.lucene.spatial.base.io.geonames.GeonamesReader;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.io.sample.SampleDataWriter;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;


public class SampleDataLoader
{


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
      int cnt = 0;
      File fout = new File( "c:/temp/worldcities-points.txt" );
      SampleDataWriter out = new SampleDataWriter( fout );
      GeonamesReader reader = new GeonamesReader( file );
      while( reader.hasNext() ) {
        Geoname place = reader.next();
        if( place.population > 150000 ) {
          System.out.println( "INCLUDE: " + place.population + "  : " + place.name );
          out.write( "G"+place.id , place.name, place.longitude, place.latitude );
          cnt++;
        }
        else {
          System.out.println( "SKIP: " + place.population + "  : " + place.name );
        }
      }
      out.close();

      System.out.println( "done: "+cnt );
      return;
    }


    SolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
  //  SolrServer solr = new CommonsHttpSolrServer( "http://localhost:8080/solr" );

    load( solr );
    System.out.println( "done." );
  }
}
