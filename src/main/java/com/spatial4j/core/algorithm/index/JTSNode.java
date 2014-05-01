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

/**
 * This code will hopefully be useful in optimizing the point in polygon algorithm. I am keeping
 * this implementation here for now.
 */

/**
 * A JTSNode represents a range which is a subsegment of the larger range represented by
 * a JTSBinaryTree used in search. This implementation is adapted for use with Geodesic ranges
 * from JTS 2D implementation
 */
public class JTSNode extends JTSNodeBase {

    // Data
    private RealGeoRange range;
    private double center;
    private int level;

    /**
     * Create a JTS node given a range
     */
    public static JTSNode createNode(RealGeoRange range) {
        JTSKey key = new JTSKey(range);
        JTSNode node = new JTSNode(key.getRange(), key.getLevel());
        return node;
    }

    /**
     * Create a JTS node expanded to a new range from a previous range
     */
    public static JTSNode createExpanded(JTSNode node, RealGeoRange range) {

        RealGeoRange currRange = null;
        if (node != null) {
            currRange = node.getRange();
            currRange.addPoint(range.getMin());
            currRange.addPoint(range.getMax());
        }

        JTSNode largerNode = createNode(currRange);
        if (node != null) largerNode.insert(node);
        return largerNode;
    }

    /**
     * Create a new JTS node from a given range and computed level
     */
    public JTSNode(RealGeoRange range, int level) {
        this.range = range;
        this.level = level;
        center = range.getCenter();
    }

    /**
     * Get the range represented by this node
     */
    public RealGeoRange getRange() { return this.range; }

    /**
     * Determine if this range matches the query range
     */
    protected boolean isSearchMatch(RealGeoRange range) {
        return this.range.contains(range);
    }

    /**
     * Return subnode containing the range
     */
    public JTSNode getNode(RealGeoRange searchRange) {
        int subNodeIndex = getSubNodeIndex(searchRange, this.center);

        if ( subNodeIndex != -1 ) {
            JTSNode node = getSubnode(subNodeIndex);
            return node.getNode(searchRange);
        } else {
            return this;
        }
    }

    /**
     * Return the smallest node containing the real geo range
     */
    public JTSNodeBase find(RealGeoRange searchRange) {
        int subnodeIndex = getSubNodeIndex(searchRange, this.center);
        if (subnodeIndex == -1) {
            return this;
        }
        if (subnode[subnodeIndex] != null) {
            JTSNode node = subnode[subnodeIndex];
            return node.find(searchRange);
        }
        return this;
    }

    /**
     * Insert a node into this existing node
     */
    public void insert(JTSNode node) {

       assert(range == null || range.contains(node.getRange()));

        int index = getSubNodeIndex(node.getRange(), this.center);
       if ( node.level == level -1 ) {
           subnode[index] = node;
       } else {
           JTSNode child = createSubnode(index);
           child.insert(node);
           subnode[index] = child;
       }
    }

    /**
     * Get the subnode for the index
     */
    private JTSNode getSubnode( int index ) {

        if (subnode[index] == null) {
            subnode[index] = createSubnode(index);
        }
        return subnode[index];
    }


    /**
     * Create a new subnode
     */
    private JTSNode createSubnode(int index) {

        double min = 0.0;
        double max = 0.0;

        switch (index) {
            case 0:
                min = range.getMin();
                max = center;
                break;
            case 1:
                min = center;
                max = range.getMax();
                break;
        }
        RealGeoRange subInt = new RealGeoRange(min, max);
        JTSNode node = new JTSNode(subInt, level - 1);
        return node;
    }

}
