package org.apache.solr.spatial.demo;

import java.io.File;

import org.apache.lucene.spatial.base.io.geonames.Geoname;
import org.apache.lucene.spatial.base.io.geonames.GeonamesReader;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.countries.CountryReader;
import org.apache.solr.spatial.demo.utils.geoeye.GeoeyeReader;


public class SampleDataLoader
{
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
      CountryReader.indexCountries( solr, file );
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
    SolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
  //  SolrServer solr = new CommonsHttpSolrServer( "http://localhost:8080/solr" );

    load( solr );
    System.out.println( "done." );
  }
}
