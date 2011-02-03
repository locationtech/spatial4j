
package voyager.quads.strtree;

import com.vividsolutions.jts.geom.Envelope;


public class Item extends Boundable
{
  public final String item;

  public Item(Envelope bounds, String item) {
    super( bounds );
    this.item = item;
  }

  public String getItem() { return item; }
}
