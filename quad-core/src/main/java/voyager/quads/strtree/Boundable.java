
package voyager.quads.strtree;

import com.vividsolutions.jts.geom.Envelope;


public class Boundable
{
  protected final Envelope bounds;

  public Boundable(Envelope bounds) {
    this.bounds = bounds;
  }

  public Envelope getBounds() {
    return bounds;
  }
}
