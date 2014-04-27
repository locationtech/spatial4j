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

import com.spatial4j.core.shape.impl.RealGeoRange;

public class JTSRoot extends JTSNodeBase {

    // the singleton root node is centred at the origin.
    private static final double origin = 0.0;

    public JTSRoot() {}

    /**
     * Insert an item into the tree this is the root of.
     */
    public void insert(RealGeoRange itemRange, Object item)
    {
        int index = getSubnodeIndex(itemRange, origin);
        // if index is -1, itemEnv must contain the origin.
        if (index == -1) {
            add(item);
            return;
        }
        /**
         * the item must be contained in one interval, so insert it into the
         * tree for that interval (which may not yet exist)
         */
        JTSNode node = subnode[index];
        /**
         *  If the subnode doesn't exist or this item is not contained in it,
         *  have to expand the tree upward to contain the item.
         */

        if (node == null || ! node.getRange().contains(itemRange)) {
            JTSNode largerNode = JTSNode.createExpanded(node, itemRange);
            subnode[index] = largerNode;
        }
        /**
         * At this point we have a subnode which exists and must contain
         * contains the env for the item.  Insert the item into the tree.
         */
        insertContained(subnode[index], itemRange, item);
    }

    /**
     * insert an item which is known to be contained in the tree rooted at
     * the given Node.  Lower levels of the tree will be created
     * if necessary to hold the item.
     */
    private void insertContained(JTSNode tree, RealGeoRange itemRange, Object item)
    {
       assert(tree.getRange().contains(itemRange));
        /**
         * Do NOT create a new node for zero-area intervals - this would lead
         * to infinite recursion. Instead, use a heuristic of simply returning
         * the smallest existing node containing the query
         */
        boolean isZeroArea = itemRange.isEmpty();
        JTSNodeBase node;
        if (isZeroArea)
            node = tree.find(itemRange);
        else
            node = tree.getNode(itemRange;
        node.add(item);
    }

    /**
     * The root node matches all searches
     */
    protected boolean isSearchMatch(RealGeoRange range)
    {
        return true;
    }


}
