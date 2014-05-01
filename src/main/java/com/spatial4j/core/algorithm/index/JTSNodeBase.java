package com.spatial4j.core.algorithm.index;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.spatial4j.core.shape.impl.RealGeoRange;

/**
 * This code will hopefully be useful in optimizing the point in polygon algorithm. I am keeping
 * this implementation here for now.
 */

/**
 * Base class for a node in a binary tree for indexing. This is a JTS implementation
 * adapted to use geodesic ranges instead of straight intervals
 */
public abstract class JTSNodeBase {

    protected List items = new ArrayList();
    protected JTSNode[] subnode = new JTSNode[2];

    /**
     * Return index of the sub-node that contains a given interval. If not, return -1.
     */
    public static int getSubNodeIndex( RealGeoRange range, double center ) {

        int subnodeIndex = -1;

        if (range.getMin() >= center) subnodeIndex = 1;
        if (range.getMax() <= center) subnodeIndex = 0;

        return subnodeIndex;
    }

    /**
     * Constructor
     */
    public JTSNodeBase() {}

    /**
     * Get List of items
     */
    public List getItems() { return items; }

    /**
     * Add item to the node base
     */
    public void add( Object item ) {
        items.add(item);
    }

    public List addAllItems(List items)
    {
        items.addAll(this.items);
        for (int i = 0; i < 2; i++) {
            if (subnode[i] != null) {
                subnode[i].addAllItems(items);
            }
        }
        return items;
    }

    protected abstract boolean isSearchMatch(RealGeoRange range);

    /**
     * Adds items in the tree which potentially overlap the query interval
     * to the given collection. If query interval is null, add all items
     */
    public void addAllItemsFromOverlapping(RealGeoRange range, Collection resultItems)
    {
        if (range != null && ! isSearchMatch(range))
            return;

        // some of these may not actually overlap - this is allowed by the bintree contract
        resultItems.addAll(items);

        if (subnode[0] != null) subnode[0].addAllItemsFromOverlapping(range, resultItems);
        if (subnode[1] != null) subnode[1].addAllItemsFromOverlapping(range, resultItems);
    }

    public boolean hasChildren()
    {
        for (int i = 0; i < 2; i++) {
            if (subnode[i] != null)
                return true;
        }
        return false;
    }

    public boolean hasItems() { return ! items.isEmpty(); }

    int depth()
    {
        int maxSubDepth = 0;
        for (int i = 0; i < 2; i++) {
            if (subnode[i] != null) {
                int sqd = subnode[i].depth();
                if (sqd > maxSubDepth)
                    maxSubDepth = sqd;
            }
        }
        return maxSubDepth + 1;
    }

    int size()
    {
        int subSize = 0;
        for (int i = 0; i < 2; i++) {
            if (subnode[i] != null) {
                subSize += subnode[i].size();
            }
        }
        return subSize + items.size();
    }

    int nodeSize()
    {
        int subSize = 0;
        for (int i = 0; i < 2; i++) {
            if (subnode[i] != null) {
                subSize += subnode[i].nodeSize();
            }
        }
        return subSize + 1;
    }

}
