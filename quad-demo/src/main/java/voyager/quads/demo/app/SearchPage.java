package voyager.quads.demo.app;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import voyager.quads.SpatialGrid;
import voyager.quads.utils.KMLHelper;
import de.micromata.opengis.kml.v_2_2_0.Kml;


public class SearchPage extends WebPage
{
  // Dirty Dirty Dirty Hack...
  static final SpatialGrid grid = new SpatialGrid( -180, 180, -90-180, 90, 16 );
  static final SolrServer solr;
  static {
    SolrServer s = null;
    try {
      s = new CommonsHttpSolrServer( "http://localhost:8080/solr" );
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    finally {
      solr = s;
    }
  }

  final IModel<Query> query = new Model<Query>( new Query() );
  final IModel<QueryResponse> queryResponse;
  final WebMarkupContainer results;

  public SearchPage(final PageParameters parameters)
  {
    add( new BookmarkablePageLink<Void>( "playground", PlaygroundPage.class ) );

    Form<Query> searchForm = new Form<Query>( "search", new CompoundPropertyModel<Query>(query) );
    searchForm.add( new TextField<String>( "text" ) );
    searchForm.add( new TextField<String>( "geo" ) );
    add( searchForm );

    queryResponse = new LoadableDetachableModel<QueryResponse>() {
      @Override
      protected QueryResponse load() {
        try {
          return solr.query( query.getObject().toSolrQuery( 100 ) );
        }
        catch (SolrServerException e) {
          throw new RuntimeException( e );
        }
      }
    };


    results = new WebMarkupContainer( "results", queryResponse ) {
      @Override
      protected void onBeforeRender()
      {
        RepeatingView rv = new RepeatingView( "item" );
        replace( rv );
        for( SolrDocument doc : queryResponse.getObject().getResults() ) {
          final String id = (String)doc.getFieldValue( "id" );
          WebMarkupContainer row = new WebMarkupContainer( rv.newChildId() );
          row.add( new Label( "name", (String)doc.getFieldValue( "name" ) ) );

          row.add( new Link<Void>( "kml" ) {
            @Override
            public void onClick() {
              StringWriter out = new StringWriter();
              Kml kml = getKML( id );
              kml.marshal( out );
              final String name = kml.getFeature().getName();
              IResourceStream resourceStream = new StringResourceStream(
                  out.getBuffer(), "application/vnd.google-earth.kml+xml" );
              getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(resourceStream)
              {
                @Override
                public String getFileName()
                {
                  return name+".kml";
                }

                @Override
                public void respond(RequestCycle requestCycle)
                {
                  super.respond(requestCycle);
                }
              });
            }
          });
          rv.add( row );
        }
        super.onBeforeRender();
      }
    };
    results.add( new WebMarkupContainer( "item" ) ); // will get replaced
    results.add( new Label("count", new AbstractReadOnlyModel<String>() {
      @Override
      public String getObject() {
        SolrDocumentList docs = queryResponse.getObject().getResults();
        return docs.getStart() + " - " + (docs.getStart()+docs.size()) + " of " + docs.getNumFound();
      }
    }));
    results.add( new WebMarkupContainer( "solr" ).add( new AttributeModifier( "href", true, new AbstractReadOnlyModel<CharSequence>() {
      @Override
      public CharSequence getObject() {
        StringBuilder url = new StringBuilder();
        url.append( "http://localhost:8080/solr/select?debugQuery=true" );
        SolrParams params = query.getObject().toSolrQuery( 10 );

        for(Iterator<String> it=params.getParameterNamesIterator(); it.hasNext(); ) {
          final String name = it.next();
          try {
            final String [] values = params.getParams(name);
            if( values.length > 0 ) {
              for( String v : values ) {
                url.append( "&"+URLEncoder.encode( name, "UTF-8" ) );
                url.append( '=' );
                url.append( URLEncoder.encode( v, "UTF-8" ) );
              }
            }
            else {
              url.append( "&"+URLEncoder.encode( name, "UTF-8" ) );
            }
          }
          catch( Exception ex ) {
            ex.printStackTrace();
          }
        }
        return url;
      }
    })));

    add( results );
  }

  public Kml getKML( String id )
  {
    try {
      QueryResponse rsp = solr.query( new SolrQuery( "id:"+id ).setFields( "geo,name" ) );
      SolrDocumentList docs = rsp.getResults();
      if( docs.size() > 0 ) {
        String cells = (String)docs.get(0).get( "geo" );
        String name = (String)docs.get(0).get( "name" );
        List<String> tokens = grid.parseStrings( cells );
        return KMLHelper.toKML(name, grid, tokens);
      }
    }
    catch( Exception ex ) {
      ex.printStackTrace();
    }
    return null;
  }
}
