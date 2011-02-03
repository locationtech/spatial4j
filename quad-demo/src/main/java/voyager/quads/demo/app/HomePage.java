package voyager.quads.demo.app;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Homepage
 */
public class HomePage extends WebPage {

  public HomePage(final PageParameters parameters) {

    // Add the simplest type of label
    add(new Label("message", "If you see this message wicket is properly configured and running"));

    // TODO Add your page's components here
  }
}
