/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *

package com.spatial4j.core.algorithm.index;

import com.spatial4j.core.shape.impl.RealGeoRange;

/**
 * Created with IntelliJ IDEA.
 * User: rfalford12
 * Date: 4/27/14
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class JTSBinTree {

    private Root root;

    // this binary tree works by expanding the latitude range (Y range)
    // uses real geo range. bounds are -90, 90

    /**
     * Check validity of the expanded interval
     */
    public static RealGeoRange ensureExtent( RealGeoRange range, double bound ) {

        double min = range.getMin();
        double max = range.getMax();

        // Check Range is not empty
        if ( !range.isEmpty() ) return range;

        // Otherwise pad the range and return a new range
        if ( )
    }

    private Root root;
    /**
     *  Statistics
     *
     * minExtent is the minimum extent of all items
     * inserted into the tree so far. It is used as a heuristic value
     * to construct non-zero extents for features with zero extent.
     * Start with a non-zero extent, in case the first feature inserted has
     * a zero extent in both directions.  This value may be non-optimal, but
     * only one feature will be inserted with this value.
     **/
    private double minExtent = 1.0;

    public Bintree()
    {
        root = new Root();
    }

    public int depth()
    {
        if (root != null) return root.depth();
        return 0;
    }
    public int size()
    {
        if (root != null) return root.size();
        return 0;
    }
    /**
     * Compute the total number of nodes in the tree
     *
     * @return the number of nodes in the tree
     */
    public int nodeSize()
    {
        if (root != null) return root.nodeSize();
        return 0;
    }

    public void insert(Interval itemInterval, Object item)
    {
        collectStats(itemInterval);
        Interval insertInterval = ensureExtent(itemInterval, minExtent);
//int oldSize = size();
        root.insert(insertInterval, item);
    /* DEBUG
int newSize = size();
System.out.println("BinTree: size = " + newSize + "   node size = " + nodeSize());
if (newSize <= oldSize) {
      System.out.println("Lost item!");
      root.insert(insertInterval, item);
      System.out.println("reinsertion size = " + size());
}
    */
    }

    /**
     * Removes a single item from the tree.
     *
     * @param itemEnv the Envelope of the item to be removed
     * @param item the item to remove
     * @return <code>true</code> if the item was found (and thus removed)
     */
    public boolean remove(Interval itemInterval, Object item)
    {
        Interval insertInterval = ensureExtent(itemInterval, minExtent);
        return root.remove(insertInterval, item);
    }

    public Iterator iterator()
    {
        List foundItems = new ArrayList();
        root.addAllItems(foundItems);
        return foundItems.iterator();
    }

    public List query(double x)
    {
        return query(new Interval(x, x));
    }

    /**
     * Queries the tree to find all candidate items which
     * may overlap the query interval.
     * If the query interval is <tt>null</tt>, all items in the tree are found.
     *
     * min and max may be the same value
     */
    public List query(Interval interval)
    {
        /**
         * the items that are matched are all items in intervals
         * which overlap the query interval
         */
        List foundItems = new ArrayList();
        query(interval, foundItems);
        return foundItems;
    }

    /**
     * Adds items in the tree which potentially overlap the query interval
     * to the given collection.
     * If the query interval is <tt>null</tt>, add all items in the tree.
     *
     * @param interval a query nterval, or null
     * @param resultItems the candidate items found
     */
    public void query(Interval interval, Collection foundItems)
    {
        root.addAllItemsFromOverlapping(interval, foundItems);
    }

    private void collectStats(Interval interval)
    {
        double del = interval.getWidth();
        if (del < minExtent && del > 0.0)
            minExtent = del;
    }

}
