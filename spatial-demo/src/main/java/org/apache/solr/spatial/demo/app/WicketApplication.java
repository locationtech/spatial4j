package org.apache.solr.spatial.demo.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 *
 * @see voyager.quads.StartDemo.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
    /**
     * Constructor
     */
	public WicketApplication()
	{
	}

	@Override
  public Class<SearchPage> getHomePage()
	{
		return SearchPage.class;
	}

  @Override
  protected void init() {
    super.init();

    mount( new HybridUrlCodingStrategy( "search", SearchPage.class ) );
    mount( new HybridUrlCodingStrategy( "playground", PlaygroundPage.class ) );
  }
}
