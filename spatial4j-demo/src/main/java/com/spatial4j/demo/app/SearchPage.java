package com.spatial4j.demo.app;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.apache.commons.lang.time.DurationFormatUtils;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.query.SpatialArgs;
import com.spatial4j.core.query.SpatialOperation;
import com.spatial4j.core.shape.IShape;

import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.Node;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import com.spatial4j.demo.KMLHelper;
import com.spatial4j.demo.SampleDataLoader;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SearchPage extends WebPage
{
  static Logger log = LoggerFactory.getLogger( SearchPage.class );

  static SampleDataLoader loader = new SampleDataLoader();
  public static final ExecutorService pool = Executors.newCachedThreadPool();


  // Dirty Dirty Dirty Hack...
  //static final SpatialPrefixTree grid = new QuadPrefixTree( -180, 180, -90-180, 90, 16 );
  static final SpatialPrefixTree grid = new GeohashPrefixTree(JtsSpatialContext.GEO_KM,GeohashPrefixTree.getMaxLevelsPossible());
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
  final IModel<String> error = new Model<String>( null );
  final IModel<Long> elapsed = new Model<Long>( null );
  final WebMarkupContainer results;

  public SearchPage(final PageParameters parameters)
  {
    add( new BookmarkablePageLink<Void>( "playground", PlaygroundPage.class ) );

    Form<Query> searchForm = new Form<Query>( "search", new CompoundPropertyModel<Query>(query) );
    searchForm.add( new DropDownChoice<String>("source",
        Arrays.asList( "(all)",
            "world-cities-points.txt", 
            "countries-poly.txt", 
            "countries-bbox.txt", 
            "states-poly.txt", 
            "states-bbox.txt" ) ));
    searchForm.add( new TextField<String>( "fq" ) );
    searchForm.add( new DropDownChoice<String>("field",
        Arrays.asList( "geo", "vector2d", "geohash", "quad", "bbox" ) ));
    searchForm.add( new DropDownChoice<SpatialOperation>("op",
        SpatialOperation.values() ));

    searchForm.add( new TextField<String>( "geo" ) );
    searchForm.add( new IndicatingAjaxButton( "submit" ) {
      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        target.addComponent( results );
      }
    });

    searchForm.add( new CheckBox( "score" ) );
    searchForm.add( new TextField<String>( "min" ) );
    searchForm.add( new TextField<String>( "max" ) );
    searchForm.add( new TextField<String>( "sort" ) );
    add( searchForm );

    queryResponse = new LoadableDetachableModel<QueryResponse>() {
      @Override
      protected QueryResponse load() {
        long now = System.currentTimeMillis();
        QueryResponse rsp = null;
        error.setObject( null );
        try {
          rsp = solr.query( query.getObject().toSolrQuery( 100 ) );
        }
        catch (SolrServerException ex) {
          Throwable t = ex.getCause();
          if( t == null ) {
            t = ex;
          }
          log.warn( "unable to execute query", ex );
          error.setObject( Throwables.getStackTraceAsString(t) );
        }
        catch (Throwable ex) {
          log.warn( "unable to execute query", ex );
          error.setObject( Throwables.getStackTraceAsString(ex) );
        }
        elapsed.setObject( System.currentTimeMillis()-now );
        return rsp;
      }
    };


    results = new WebMarkupContainer( "results", queryResponse ) {
      @Override
      protected void onBeforeRender()
      {
        RepeatingView rv = new RepeatingView( "item" );
        replace( rv );

        QueryResponse rsp = queryResponse.getObject();
        if( rsp != null ) {
          for( SolrDocument doc : rsp.getResults() ) {
            final String id = (String)doc.getFieldValue( "id" );
            WebMarkupContainer row = new WebMarkupContainer( rv.newChildId() );
            row.add( new Label( "name", doc.getFieldValue( "id" ) + " - " + doc.getFieldValue( "name" ) ) );
            row.add( new Label( "source", (String)doc.getFieldValue( "source" ) ));
            row.add( new Label( "score", doc.getFieldValue( "score" )+"" ));
            row.add( new ExternalLink( "link", "/solr/select?q=id:"+ClientUtils.escapeQueryChars(id) ));

            row.add( addKmlLink( "kml", id, "geohash" ));
            rv.add( row );
          }
        }
        super.onBeforeRender();
      }
    };
    results.setOutputMarkupId( true );
    results.add( new WebMarkupContainer( "item" ) ); // will get replaced
    results.add( new Label("count", new AbstractReadOnlyModel<String>() {
      @Override
      public String getObject() {
        QueryResponse rsp = queryResponse.getObject();
        String v = null;
        if( rsp == null ) {
          v = "unable to execute query";
        }
        else {
          SolrDocumentList docs = rsp.getResults();
          v = docs.getStart() + " - " + (docs.getStart()+docs.size()) + " of " + docs.getNumFound();
        }
        v += " ["+DurationFormatUtils.formatDurationHMS(elapsed.getObject())+"]";
        return v;
      }
    }));
    results.add( new Label( "solr", new AbstractReadOnlyModel<String>() {

      @Override
      public String getObject() {
        SolrParams params = query.getObject().toSolrQuery( 10 );
        return params.get( "q" );
      }

    }).add( new AttributeModifier( "href", true, new AbstractReadOnlyModel<CharSequence>() {
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
    results.add( new Label( "error", error ) {
      @Override
      public boolean isVisible() {
        return error.getObject() != null;
      }
    });

    add( results );

    final WebMarkupContainer load = new WebMarkupContainer( "load" );
    load.setOutputMarkupId( true );
    load.add( new IndicatingAjaxLink<Void>( "link" ) {
      @Override
      public void onClick(AjaxRequestTarget target) {
        pool.execute( new Runnable() {
          @Override
          public void run() {
            try {
              File data = new File("data");
              String dir = System.getProperty("data.dir");
              if(dir!=null) {
                data = new File(dir);
              }
              
              SolrServer sss = new StreamingUpdateSolrServer(
                  "http://localhost:8080/solr", 50, 3 );
              
              // single thread
             // sss = new CommonsHttpSolrServer("http://localhost:8080/solr");
              
              loader.loadSampleData( data, sss );
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

        load.add( new AbstractAjaxTimerBehavior( Duration.seconds(1) ) {
          @Override
          protected void onTimer(AjaxRequestTarget target) {
            System.out.println( "running: "+loader.status );
            if( !loader.running ) {
              this.stop();
              setResponsePage( getPage() );
            }
            target.addComponent( load );
          }
        });

        try {
          Thread.sleep( 100 );
        } catch (InterruptedException e) {}
        target.addComponent( load );
      }

      @Override
      public boolean isVisible() {
        return !loader.running;
      }
    });
    WebMarkupContainer status = new WebMarkupContainer( "status" ) {
      @Override
      public boolean isVisible() {
        return loader.running;
      }
    };
    load.add( status );
    status.add( new Label( "history", new AbstractReadOnlyModel<CharSequence>() {
      @Override
      public CharSequence getObject() {
        StringBuilder str = new StringBuilder();
        for( String line : loader.history ) {
          str.append( line ).append( "\n" );
        }
        return str;
      }
    }));
    status.add( new Label( "status", new AbstractReadOnlyModel<CharSequence>() {
      @Override
      public CharSequence getObject() {
        return "Loading: "+loader.name+" ("+loader.count + ") " + loader.status;
      }
    }));
    add( load );
  }
  
  public Link<Void> addKmlLink( String id, final String docID, final String field ) {
    return new Link<Void>( "kml" ) {
      @Override
      public void onClick() {
        StringWriter out = new StringWriter();
        Kml kml = getKML( docID, field );
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
    };
  }

  public Kml getKML( String id, String field )
  {
    try {
      QueryResponse rsp = solr.query( new SolrQuery( "id:"+id ).setFields( "name,"+field ) );
      SolrDocumentList docs = rsp.getResults();
      if( docs.size() > 0 ) {
        String name = (String)docs.get(0).get( "name" );

        // for multi valued fields, just use the first...
        String shapeString = (String)docs.get(0).getFirstValue( field );

        IShape shape = grid.getSpatialContext().readShape(shapeString);
        int detailLevel = grid.getMaxLevelForPrecision(shape, SpatialArgs.DEFAULT_DIST_PRECISION);
        List<Node> cells = grid.getNodes(shape, detailLevel, false);//false = no intermediates
        List<String> tokens = SpatialPrefixTree.nodesToTokenStrings(cells);
        return KMLHelper.toKML(name, grid, tokens);
      }
    }
    catch( Exception ex ) {
      ex.printStackTrace();
    }
    return null;
  }
}
