package org.apache.solr.spatial.demo.app;

import java.io.Serializable;

import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;

public class Query implements Serializable
{
  public String fq;

  public String field = "geohash";
  public SpatialOperation op = SpatialOperation.IsWithin;
  public String geo;

  public Boolean cache;
  public Boolean score;
  public String distance;
  public String radius;
  public String sort;

  public SolrParams toSolrQuery( int rows )
  {
    SolrQuery params = new SolrQuery();
    String q = "";

    boolean hasGeo = (geo != null && geo.length() > 0);
    if( hasGeo ) {
      q = field + ":\""+op.name()+"("+geo+")";
      if( distance != null ) {
        q += " distance=" + distance;
      }
      if( radius != null ) {
        q += " radius=" + radius;
      }
      if( cache != null ) {
        q += " cache=" + cache;
      }
      if( score != null ) {
        q += " score=" + score;
      }
      q += '"';
    }
    else {
      q = "*:*";
    }
    if( fq != null ) {
      params.setFilterQueries( fq );
    }

    // Set sort
    if( sort != null ) {
      params.set( CommonParams.SORT, sort );
    }

    params.setQuery( q );
    params.setRows( rows );
    params.setFields( "id,name,source,score" );
    return params;
  }
}
