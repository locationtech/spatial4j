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
 * Unique key used to identify a node in a JTS Binary Tree. Adapted from
 * a 2D implementation in JTS to be used with geodesic ranges
 */
public class JTSKey {

    // Data
    private RealGeoRange range;
    private double pt = 0.0;
    private int level = 0;

    /**
     * Constructor
     */
    public JTSKey(RealGeoRange range) {
        computeKey(range);
    }

    /**
     * Return the point described by this key
     */
    public double getPoint() { return this.pt; }

    /**
     * Return the level of this key in the binary tree
     */
    public int getLevel() { return this.level; }

    /**
     * Return the interval described by this key
     */
    public RealGeoRange getRange() { return range; }

    /**
     * Compute key
     */
    public void computeKey(RealGeoRange newRange) {
        level = computeLevel(newRange);
        range = new RealGeoRange();
        computeInterval(level, range);

        while (! range.contains(newRange))  {
            level += 1;
            computeInterval(level, newRange);
        }
    }

    public static int computeLevel( RealGeoRange range ) {
        double d = range.getLength();

        // compute level
        long x = Double.doubleToLongBits(d);
        int signExp = (int) (x >> 52);
        int level = signExp & 0x07ff;

        return level;
    }

    private void computeInterval(int level, RealGeoRange newRange) {

        // Compute size using double bits
        long expBias = level + 1023;
        long bits = (long) expBias << 52;
        double size = Double.longBitsToDouble(bits);

        pt = Math.floor(newRange.getMin() / size) * size;
        range = new RealGeoRange(pt, pt + size);

    }

}
