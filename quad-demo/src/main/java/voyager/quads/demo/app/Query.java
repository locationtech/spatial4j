package voyager.quads.demo.app;

import java.io.Serializable;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;

public class Query implements Serializable
{
  public String text;
  public String geo;

  public SolrParams toSolrQuery()
  {
    SolrQuery q = new SolrQuery();
    if( text == null || text.length() < 1 ) {
      q.setQuery( "*:*" );
    }
    else {
      q.setQuery( text );
    }

    if( geo != null && geo.length() > 0 ) {
      q.addFilterQuery( "geo:"+ClientUtils.escapeQueryChars( geo ) );
    }

    q.setRows( 100 );
    q.setFields( "id,name" );
    return q;
  }
}
