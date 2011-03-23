package org.apache.solr.spatial.demo.app;

import java.io.Serializable;

import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.SolrParams;

public class Query implements Serializable
{
  public String fq;

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
    else {
      q = "*:*";
    }
    if( fq != null ) {
      params.setFilterQueries( fq );
    }

    params.setQuery( q );
    params.setRows( rows );
    params.setFields( "id,name,source,score" );
    return params;
  }
}
