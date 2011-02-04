package voyager.quads.demo.app;

import java.io.File;

import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

import voyager.quads.utils.countries.CountryReader;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
  public HomePage(final PageParameters parameters)
  {
    add(new Label("message", "If you see this message wicket is properly configured and running"));

    add( new Link<Void>( "countries" ) {
      @Override
      public void onClick() {
        File file = new File( "../data/countries/cntry06.shp" );
        System.out.println( "indexing: "+file.getAbsolutePath() );

        try {
          StreamingUpdateSolrServer solr = new StreamingUpdateSolrServer( "http://localhost:8080/solr", 50, 3 );
          CountryReader.indexCountries( solr, file );
          solr.commit();
        }
        catch( Exception ex ) {
          throw new RuntimeException( ex );
        }
      }
    });
  }
}
