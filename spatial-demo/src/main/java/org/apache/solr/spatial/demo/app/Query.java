package org.apache.solr.spatial.demo.app;

import java.io.Serializable;

import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.SolrParams;

public class Query implements Serializable
{
  public String text;

  public String field = "geo";
  public SpatialOperation op = SpatialOperation.IsWithin;
  public String geo;
  public String extra;


  public SolrParams toSolrQuery( int rows )
  {
    SolrQuery params = new SolrQuery();
    String q = "";

    boolean hasGeo = (geo != null && geo.length() > 0);
    if( hasGeo ) {
      q = field + ":\""+op.name()+"("+geo+")";
      if( extra != null ) {
        q += " " + extra;
      }
      q += '"';
    }

    if( text != null ) {
      q += " " + text;
    }

    if( q.length() < 2 ) {
      q = "*:*";
    }

    params.setQuery( q );
    params.setRows( rows );
    params.setFields( "id,name,geo,grid" );
    return params;
  }
}
