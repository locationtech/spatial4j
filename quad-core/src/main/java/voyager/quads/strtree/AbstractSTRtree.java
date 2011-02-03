
package voyager.quads.strtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.SIRtree;
import com.vividsolutions.jts.util.Assert;

/**
 * Base class for STRtree and SIRtree. STR-packed R-trees are described in:
 * P. Rigaux, Michel Scholl and Agnes Voisard. Spatial Databases With
 * Application To GIS. Morgan Kaufmann, San Francisco, 2002.
 * <p>
 * This implementation is based on Boundables rather than just AbstractNodes,
 * because the STR algorithm operates on both nodes and
 * data, both of which are treated here as Boundables.
 *
 * @see STRtree
 * @see SIRtree
 *
 * @version 1.7
 */
public abstract class AbstractSTRtree
{
  /**
   * A test for intersection between two bounds, necessary because subclasses
   * of AbstractSTRtree have different implementations of bounds.
   */
  protected static interface IntersectsOp {
    /**
     * For STRtrees, the bounds will be Envelopes; for SIRtrees, Intervals;
     * for other subclasses of AbstractSTRtree, some other class.
     * @param aBounds the bounds of one spatial object
     * @param bBounds the bounds of another spatial object
     * @return whether the two bounds intersect
     */
    boolean intersects(Envelope aBounds, Envelope bBounds);
  }

  protected Node root;

  private boolean built = false;
  private ArrayList<Boundable> itemBoundables = new ArrayList<Boundable>();
  private int nodeCapacity;

  /**
   * Constructs an AbstractSTRtree with the specified maximum number of child
   * nodes that a node may have
   */
  public AbstractSTRtree(int nodeCapacity) {
    Assert.isTrue(nodeCapacity > 1, "Node capacity must be greater than 1");
    this.nodeCapacity = nodeCapacity;
  }

  /**
   * Creates parent nodes, grandparent nodes, and so forth up to the root
   * node, for the data that has been inserted into the tree. Can only be
   * called once, and thus can be called only after all of the data has been
   * inserted into the tree.
   */
  public void build() {
    Assert.isTrue(!built);
    root = itemBoundables.isEmpty()
           ? createNode(0)
           : createHigherLevels(itemBoundables, -1);
    built = true;
  }

  protected abstract Node createNode(int level);

  /**
   * Sorts the childBoundables then divides them into groups of size M, where
   * M is the node capacity.
   */
  protected List<Boundable> createParentBoundables(List<Boundable> childBoundables, int newLevel) {
    Assert.isTrue(!childBoundables.isEmpty());
    ArrayList<Boundable> parentBoundables = new ArrayList<Boundable>();
    parentBoundables.add(createNode(newLevel));
    ArrayList<Boundable> sortedChildBoundables = new ArrayList<Boundable>(childBoundables);
    Collections.sort(sortedChildBoundables, getComparator());

    for (Boundable childBoundable : sortedChildBoundables ) {
      if (lastNode(parentBoundables).getChildren().size() == getNodeCapacity()) {
        parentBoundables.add(createNode(newLevel));
      }
      lastNode(parentBoundables).addChildBoundable(childBoundable);
    }
    return parentBoundables;
  }

  protected Node lastNode(List nodes) {
    return (Node) nodes.get(nodes.size() - 1);
  }

  protected int compareDoubles(double a, double b) {
    return a > b ? 1
         : a < b ? -1
         : 0;
  }

  /**
   * Creates the levels higher than the given level
   *
   * @param boundablesOfALevel
   *            the level to build on
   * @param level
   *            the level of the Boundables, or -1 if the boundables are item
   *            boundables (that is, below level 0)
   * @return the root, which may be a ParentNode or a LeafNode
   */
  private Node createHigherLevels(List<Boundable> boundablesOfALevel, int level) {
    Assert.isTrue(!boundablesOfALevel.isEmpty());
    List<Boundable> parentBoundables = createParentBoundables(boundablesOfALevel, level + 1);
    if (parentBoundables.size() == 1) {
      return (Node) parentBoundables.get(0);
    }
    return createHigherLevels(parentBoundables, level + 1);
  }

  public Node getRoot()
  {
    if (! built) build();
    return root;
  }

  /**
   * Returns the maximum number of child nodes that a node may have
   */
  public int getNodeCapacity() { return nodeCapacity; }

  protected int size() {
    if (!built) { build(); }
    if (itemBoundables.isEmpty()) {
      return 0;
    }
    return size(root);
  }

  protected int size(Node node)
  {
    int size = 0;
    for(Boundable childBoundable : node.getChildren() ) {
      if (childBoundable instanceof Node) {
        size += size((Node) childBoundable);
      }
      else if (childBoundable instanceof Item) {
        size += 1;
      }
    }
    return size;
  }

  protected int depth() {
    if (!built) { build(); }
    if (itemBoundables.isEmpty()) {
      return 0;
    }
    return depth(root);
  }

  protected int depth(Node node)
  {
    int maxChildDepth = 0;
    for(Boundable childBoundable : node.getChildren() ) {
      if (childBoundable instanceof Node) {
        int childDepth = depth((Node) childBoundable);
        if (childDepth > maxChildDepth)
          maxChildDepth = childDepth;
      }
    }
    return maxChildDepth + 1;
  }


  protected void insert(Envelope bounds, String item) {
    Assert.isTrue(!built, "Cannot insert items into an STR packed R-tree after it has been built.");
    itemBoundables.add(new Item(bounds, item));
  }

  /**
   *  Also builds the tree, if necessary.
   */
  protected void query(Envelope searchBounds, ItemVisitor visitor) {
    if (!built) { build(); }
    if (itemBoundables.isEmpty()) {
      Assert.isTrue(root.getBounds() == null);
    }
    if (getIntersectsOp().intersects(root.getBounds(), searchBounds)) {
      query(searchBounds, root, visitor);
    }
  }

  /**
   * @return a test for intersection between two bounds, necessary because subclasses
   * of AbstractSTRtree have different implementations of bounds.
   * @see IntersectsOp
   */
  protected abstract IntersectsOp getIntersectsOp();

  private void query(Envelope searchBounds, Node node, ItemVisitor visitor) {
    for( Boundable childBoundable : node.getChildren() ) {
      if (!getIntersectsOp().intersects(childBoundable.getBounds(), searchBounds) ) {
        continue;
      }
      if (childBoundable instanceof Node) {
        query(searchBounds, (Node) childBoundable, visitor);
      }
      else if (childBoundable instanceof Item) {
        visitor.visitItem( ((Item)childBoundable).item );
      }
      else {
        Assert.shouldNeverReachHere();
      }
    }
  }


  protected List<Boundable> boundablesAtLevel(int level) {
    ArrayList<Boundable> boundables = new ArrayList<Boundable>();
    boundablesAtLevel(level, root, boundables);
    return boundables;
  }

  /**
   * @param level -1 to get items
   */
  private void boundablesAtLevel(int level, Node top, Collection<Boundable> boundables) {
    Assert.isTrue(level > -2);
    if (top.getLevel() == level) {
      boundables.add(top);
      return;
    }
    for (Boundable boundable : top.getChildren() ) {
      if (boundable instanceof Node) {
        boundablesAtLevel(level, (Node)boundable, boundables);
      }
      else {
        Assert.isTrue(boundable instanceof Item);
        if (level == -1) { boundables.add(boundable); }
      }
    }
    return;
  }

  protected abstract Comparator<Boundable> getComparator();

}
