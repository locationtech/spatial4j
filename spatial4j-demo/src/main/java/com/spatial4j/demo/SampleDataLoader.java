package com.spatial4j.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.io.sample.SampleData;
import com.spatial4j.core.io.sample.SampleDataReader;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SampleDataLoader
{
  static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);
  
  public boolean running = false;
  public List<String> history = new ArrayList<String>();
  public String name = null;
  public String status = null;
  public int count = 0;

  public void index( SolrServer solr, String name, String sfix, SampleDataReader reader ) throws Exception {
    this.name = name;
    count = 0;
    while( reader.hasNext() ) {
      SampleData data = reader.next();
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", data.id+sfix );
      doc.setField( "name", data.name );
      doc.setField( "geo", data.shape );
      doc.setField( "source", name );
      solr.add( doc );
      count++;
      this.status = data.name;
    }
    this.status = "commit...";
    solr.commit( true, true );
    history.add( "Loaded: "+name+ " ["+count+"]" );
    this.status = "done.";
  }

  public void loadSampleData( File directory, SolrServer solr ) throws Exception
  {
    status = "initalizing....";
    running = true;
    String[][] names = new String[][] {
      new String[] { "world-cities-points.txt", "" },
      new String[] { "countries-poly.txt", "_poly" },
      new String[] { "countries-bbox.txt", "_bbox" },
      new String[] { "states-poly.txt", "_poly" },
      new String[] { "states-bbox.txt", "_bbox" },
    };

    for( String[] d : names ) {
      try {
        File f = new File(directory,d[0]);
        if(f.exists()) {
          InputStream in = new FileInputStream(f);
          index(solr, d[0], d[1], new SampleDataReader( in ) );
        }
        else {
          status = "File not found: "+f.getAbsolutePath();
          history.add(status);
          log.error(status);
        }
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }

    status = "done.";
    running = false;
//
//    // Geonames
//    file = new File( "../data/geonames/US.txt" );
//    if( false && file.exists() ) {
//      GeonamesReader reader = new GeonamesReader( file );
//      while( reader.hasNext() ) {
//        Geoname name = reader.next();
//        SolrInputDocument doc = new SolrInputDocument();
//        doc.setField( "id", name.id+"" );
//        doc.setField( "name", name.name+"" );
//        doc.setField( "geo", name.longitude + " " + name.latitude );
//        doc.setField( "source", "geonames-"+file.getName() );
//        solr.add( doc );
//
//        if( (reader.getCount() % 1000) == 0 ) {
//          System.out.println( "geonames: "+reader.getCount() + " :: " + name.name );
//        }
//      }
//      solr.commit( true, true );
//    }
  }
}
