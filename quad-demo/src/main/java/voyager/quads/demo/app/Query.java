package voyager.quads.demo.app;

import java.io.Serializable;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;

public class Query implements Serializable
{
  public String text;
  public String geo;

  public SolrParams toSolrQuery( int rows )
  {
    SolrQuery params = new SolrQuery();
    String q = text;

    boolean hasGeo = (geo != null && geo.length() > 0);

    if( q == null || q.length() < 1 ) {
      if( hasGeo ) {
        q = "geo:"+ClientUtils.escapeQueryChars( geo );
      }
      else {
        q = "*:*";
      }
    }
    else if( hasGeo ) {
      params.addFilterQuery( "geo:"+ClientUtils.escapeQueryChars( geo ) );
    }

    params.setQuery( q );
    params.setRows( rows );
    params.setFields( "id,name" );
    return params;
  }
}
