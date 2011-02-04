package voyager.quads.demo;

import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;

import voyager.quads.utils.countries.CountryReader;
import voyager.quads.utils.geoeye.GeoeyeReader;
import voyager.quads.utils.geonames.Geoname;
import voyager.quads.utils.geonames.GeonamesReader;

public class SampleDataLoader
{
  public static void main( String[] args ) throws Exception
  {
    File file = null;
    SolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
    //SolrServer solr = new CommonsHttpSolrServer( "http://localhost:8080/solr" );
    
//    file = new File( "../data/ikonos_2011/ikonos_2011.shp" );
//    if( file.exists() ) {
//      GeoeyeReader.indexItems( solr, file );
//      solr.commit( true, true );
//    }
//    
//    file = new File( "../data/countries/cntry06.shp" );
//    if( file.exists() ) {
//      CountryReader.indexCountries( solr, file );
//      solr.commit( true, true );
//    }
    
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
        solr.add( doc );
        
        if( (reader.getCount() % 1000) == 0 ) {
          System.out.println( "geonames: "+reader.getCount() + " :: " + name.name );
        }
      }
      solr.commit( true, true );
    }

    System.out.println( "done." );
  }
}
