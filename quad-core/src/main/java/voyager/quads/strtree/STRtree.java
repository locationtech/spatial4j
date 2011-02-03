
package voyager.quads.strtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;

/**
 *  A query-only R-tree created using the Sort-Tile-Recursive (STR) algorithm.
 *  For two-dimensional spatial data.
 * <P>
 *  The STR packed R-tree is simple to implement and maximizes space
 *  utilization; that is, as many leaves as possible are filled to capacity.
 *  Overlap between nodes is far less than in a basic R-tree. However, once the
 *  tree has been built (explicitly or on the first call to #query), items may
 *  not be added or removed.
 * <P>
 * Described in: P. Rigaux, Michel Scholl and Agnes Voisard.
 * <i>Spatial Databases With Application To GIS</i>.
 * Morgan Kaufmann, San Francisco, 2002.
 *
 * @version 1.7
 */
public class STRtree extends AbstractSTRtree
{
  private final Comparator<Boundable> xComparator =
    new Comparator<Boundable>() {
      public int compare(Boundable o1, Boundable o2) {
        return compareDoubles(
            centreX(o1.getBounds()),
            centreX(o2.getBounds()));
      }
    };

  private final Comparator<Boundable> yComparator =
    new Comparator<Boundable>() {
      public int compare(Boundable o1, Boundable o2) {
        return compareDoubles(
            centreY(o1.getBounds()),
            centreY(o2.getBounds()));
      }
    };

  private double centreX(Envelope e) {
    return avg(e.getMinX(), e.getMaxX());
  }

  private double centreY(Envelope e) {
    return avg(e.getMinY(), e.getMaxY());
  }

  private double avg(double a, double b) {
    return (a + b) / 2d;
  }

  private IntersectsOp intersectsOp = new IntersectsOp() {
    public boolean intersects(Envelope aBounds, Envelope bBounds) {
      return aBounds.intersects(bBounds);
    }
  };

  /**
   * Creates the parent level for the given child level. First, orders the items
   * by the x-values of the midpoints, and groups them into vertical slices.
   * For each slice, orders the items by the y-values of the midpoints, and
   * group them into runs of size M (the node capacity). For each run, creates
   * a new (parent) node.
   */
  @Override
  protected List<Boundable> createParentBoundables(List<Boundable> childBoundables, int newLevel) {
    Assert.isTrue(!childBoundables.isEmpty());
    int minLeafCount = (int) Math.ceil((childBoundables.size() / (double) getNodeCapacity()));
    ArrayList<Boundable> sortedChildBoundables = new ArrayList<Boundable>(childBoundables);
    Collections.sort(sortedChildBoundables, xComparator);
    List<Boundable>[] verticalSlices = verticalSlices(sortedChildBoundables,
        (int) Math.ceil(Math.sqrt(minLeafCount)));
    return createParentBoundablesFromVerticalSlices(verticalSlices, newLevel);
  }

  private List<Boundable> createParentBoundablesFromVerticalSlices(List<Boundable>[] verticalSlices, int newLevel) {
    Assert.isTrue(verticalSlices.length > 0);
    List<Boundable> parentBoundables = new ArrayList<Boundable>();
    for (int i = 0; i < verticalSlices.length; i++) {
      parentBoundables.addAll( createParentBoundablesFromVerticalSlice(verticalSlices[i], newLevel));
    }
    return parentBoundables;
  }

  protected List<Boundable> createParentBoundablesFromVerticalSlice(List<Boundable> childBoundables, int newLevel) {
    return super.createParentBoundables(childBoundables, newLevel);
  }

  /**
   * @param childBoundables Must be sorted by the x-value of the envelope midpoints
   */
  protected List[] verticalSlices(List childBoundables, int sliceCount) {
    int sliceCapacity = (int) Math.ceil(childBoundables.size() / (double) sliceCount);
    List[] slices = new List[sliceCount];
    Iterator i = childBoundables.iterator();
    for (int j = 0; j < sliceCount; j++) {
      slices[j] = new ArrayList();
      int boundablesAddedToSlice = 0;
      while (i.hasNext() && boundablesAddedToSlice < sliceCapacity) {
        Boundable childBoundable = (Boundable) i.next();
        slices[j].add(childBoundable);
        boundablesAddedToSlice++;
      }
    }
    return slices;
  }

  private static final int DEFAULT_NODE_CAPACITY = 10;
  /**
   * Constructs an STRtree with the default node capacity.
   */
  public STRtree()
  {
    this(DEFAULT_NODE_CAPACITY);
  }

  /**
   * Constructs an STRtree with the given maximum number of child nodes that
   * a node may have.
   * <p>
   * The minimum recommended capacity setting is 4.
   *
   */
  public STRtree(int nodeCapacity) {
    super(nodeCapacity);
  }

  @Override
  protected Node createNode(int level) {
    return new Node(level) {
      protected Object computeBounds() {
        Envelope bounds = null;
        for (Iterator i = getChildren().iterator(); i.hasNext(); ) {
          Boundable childBoundable = (Boundable) i.next();
          if (bounds == null) {
            bounds = new Envelope(childBoundable.getBounds());
          }
          else {
            bounds.expandToInclude(childBoundable.getBounds());
          }
        }
        return bounds;
      }
    };
  }

  @Override
  protected IntersectsOp getIntersectsOp() {
    return intersectsOp;
  }

  /**
   * Inserts an item having the given bounds into the tree.
   */
  @Override
  public void insert(Envelope itemEnv, String item) {
    if (itemEnv.isNull()) { return; }
    super.insert(itemEnv, item);
  }

  /**
   * Returns items whose bounds intersect the given envelope.
   */
  @Override
  public void query(Envelope searchEnv, ItemVisitor visitor) {
    //Yes this method does something. It specifies that the bounds is an
    //Envelope. super.query takes an Object, not an Envelope. [Jon Aquino 10/24/2003]
    super.query(searchEnv, visitor);
  }

  @Override
  protected Comparator<Boundable> getComparator() {
    return yComparator;
  }
}
