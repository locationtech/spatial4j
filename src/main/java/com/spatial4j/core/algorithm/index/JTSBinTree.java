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
 */

package com.spatial4j.core.algorithm.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.spatial4j.core.shape.impl.RealGeoRange;

/**
 * This code will hopefully be useful in optimizing the point in polygon algorithm. I am keeping
 * this implementation here for now.
 */

/**
 * A Binary tree is used for easy searching of ranges and detecting intersection
 * between line segments. This implementation mirrors the original implementation of binary
 * trees over an interval in the Java Topology Suite (JTS) but here it is adapted to work with real
 * latitude ranges in a {@link RealGeoRange}
 */
public class JTSBinTree {

    // Store the root of the binary tree
    private JTSRoot root;
    private double minExtent = 1.0;

    /**
     * Check that the geo range for the inserted item has non-zero extends.
     * Use the current minExtent to pad if necessary.
     */
    public static RealGeoRange ensureExtent(RealGeoRange range, double minExtent) {

        double min = range.getMin();
        double max = range.getMax();

        if ( min != max ) return range;

        if ( min == max ) {
            min = min - minExtent / 2.0;
            max = min + minExtent / 2.0;
        }

        return new RealGeoRange(min, max);
    }

    /**
     * Construct a new binary tree with a fresh root
     */
    public JTSBinTree() {
        root = new JTSRoot();
    }

    /**
     * Get the current depth of this binary tree
     */
    public int depth()
    {
        if (root != null) return root.depth();
        return 0;
    }

    /**
     * Get the current size of this binary tree
     */
    public int size()
    {
        if (root != null) return root.size();
        return 0;
    }

    /**
     * Compute the total number of nodes in the tree
     */
    public int nodeSize()
    {
        if (root != null) return root.nodeSize();
        return 0;
    }

    /**
     * Insert a new range into the binary tree
     */
    public void insert(RealGeoRange newRange, Object item)
    {
        collectStats(newRange);
        RealGeoRange insertInterval = ensureExtent(newRange, minExtent);
        root.insert(insertInterval, item);
    }

    /**
     * Iterate through the binary tree
     */
    public Iterator iterator() {
        List foundItems = new ArrayList();
        root.addAllItems(foundItems);
        return foundItems.iterator();
    }

    /**
     * Search for a value in the interval
     */
    public List query(double x) {
        return query(new RealGeoRange(x, x));
    }

    /**
     * Query the tree to find all candidate items which may overlap the query interval
     */
    public List query(RealGeoRange myRange) {
        List foundItems = new ArrayList();
        query(myRange, foundItems);
        return foundItems;
    }

    /**
     * Add items in the tree potentially overlapping the query interval
     */
    public void query(RealGeoRange range, Collection foundItems) {
        root.addAllItemsFromOverlapping(range, foundItems);
    }

    /**
     * Change minExtend based on the current interval
     */
    private void collectStats(RealGeoRange range)
    {
        double del = range.getLength();
        if (del < minExtent && del > 0.0)
            minExtent = del;
    }

}
