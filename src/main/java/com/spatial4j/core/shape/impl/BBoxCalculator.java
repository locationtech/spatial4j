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

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BBoxCalculator {
  
  SpatialContext ctx;
  double minX = Double.POSITIVE_INFINITY;
  double maxX = Double.NEGATIVE_INFINITY;
  double minY = Double.POSITIVE_INFINITY;
  double maxY = Double.NEGATIVE_INFINITY;

  TreeMap<Double, Double> ranges;
  boolean worldWrapped = false;

  public void expandRange(double minX, double maxX, double minY, double maxY) {
    this.minY = Math.min(this.minY, minY);
    this.maxY = Math.max(this.maxY, maxY);
    if (!ctx.isGeo()) {
      this.minX = Math.min(this.minX, minX);
      this.maxX = Math.max(this.maxX, maxX);
      return;
    }

    if (worldWrapped)
      return;

    if (ranges == null) {
      ranges = new TreeMap<Double, Double>();
      ranges.put(maxX, minX);
      return;
    }
    assert !ranges.isEmpty();
    //now the hard part!

    //Get a subMap where the first entry either contains minX or it's to the right.
    NavigableMap<Double,Double> iterMap = ranges.tailMap(minX, true/*inclusive*/);//Log(N)
    if (iterMap.isEmpty()) {
      iterMap = ranges;//wrapped across dateline
    }
    final Map.Entry<Double, Double> firstEntry = iterMap.firstEntry();

    Double firstEntryMin = firstEntry.getValue();
    Double firstEntryMax = firstEntry.getKey();

    //See if firstEntry contains maxX
    if (containsPt(firstEntryMin, firstEntryMax, maxX)) {

      //See if firstEntry contains minX
      if (containsPt(firstEntryMin, firstEntryMax, minX)) {

        //Done: either world-wrap or we're within an existing range
        if ( (minX != firstEntryMin || maxX != firstEntryMax) //ranges not equal
            && containsPt(minX, maxX, firstEntryMin) && containsPt(minX, maxX, firstEntryMax)) {
          worldWrapped = true;
          ranges = null;
        }
        //optimization: check for existing world-wrap
        if (firstEntryMin == -180 && firstEntryMax == 180) {
          worldWrapped = true;
          ranges = null;
        }

        return;
      }

      //Done: Update firstEntry's start to be minX
      firstEntry.setValue(minX);

    } else {//firstEntry does NOT contain maxX

      final Double newMinX;//of new range to insert

      //See if firstEntry does NOT contains minX
      if (!containsPt(firstEntryMin, firstEntryMax, minX)) {

        //if minX-MaxX doesn't cross into firstEntry, just add the range
        if (!containsPt(minX, maxX, firstEntryMin)) {
          //doesn't intersect any ranges
          ranges.put(minX, maxX);
          return;
        }
        //else remove to the right then add...
        newMinX = minX;

      } else {
        //firstEntry DOES contains minX (but not maxX)
        //remove to the right then add...
        newMinX = firstEntryMin;
      }

      Double newMaxX = maxX;
      //The remove-right loop
      iterMap.pollFirstEntry();//remove firstEntry
      while (!ranges.isEmpty()) {
        final Map.Entry<Double, Double> entry = iterMap.firstEntry();
        if (entry == null) {
          //wrap around (will only happen once)
          iterMap = ranges;
          continue;
        }
        final Double entryMin = entry.getValue();
        if (!containsPt(newMinX, newMaxX, entryMin))
          break;
        final Double entryMax = entry.getKey();
        iterMap.pollFirstEntry();//remove entry
        if (!containsPt(minX, maxX, entryMax)) {
          newMaxX = entryMax;//adjust newMaxX and stop.
          break;
        }
      }
      //Add entry
      ranges.put(newMinX, newMaxX);
    }
  }//expandRange

  private static boolean containsPt(double minX, double maxX, double x) {
    if (minX <= maxX)
      return x >= minX && x <= maxX;
    else
      return x <= minX || x >= maxX;
  }

}
