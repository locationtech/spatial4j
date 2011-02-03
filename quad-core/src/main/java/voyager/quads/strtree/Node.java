
package voyager.quads.strtree;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;

/**
 * A node of the STR tree. The children of this node are either more nodes
 * (AbstractNodes) or real data (ItemBoundables). If this node contains real data
 * (rather than nodes), then we say that this node is a "leaf node".
 *
 * @version 1.7
 */
public class Node extends Boundable
{
  private final int level;
  private final ArrayList<Boundable> children = new ArrayList<Boundable>();

  /**
   * Constructs an AbstractNode at the given level in the tree
   * @param level 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  public Node(int level) {
    super( new Envelope() );
    this.level = level;
  }

  /**
   * Returns either child {@link AbstractNodes}, or if this is a leaf node, real data (wrapped
   * in {@link ItemBoundables}).
   */
  public List<Boundable> getChildren() {
    return children;
  }

  @Override
  public Envelope getBounds() {
    if (bounds.isNull()) {
      // compute the bounds
      for( Boundable b : children ) {
        bounds.expandToInclude( b.getBounds() );
      }
    }
    return bounds;
  }

  /**
   * Returns 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  public int getLevel() {
    return level;
  }

  /**
   * Adds either an AbstractNode, or if this is a leaf node, a data object
   * (wrapped in an ItemBoundable)
   */
  public void addChildBoundable(Boundable childBoundable) {
    Assert.isTrue(bounds.isNull());
    children.add(childBoundable);
  }
}
