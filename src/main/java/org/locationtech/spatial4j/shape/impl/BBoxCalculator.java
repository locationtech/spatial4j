/*******************************************************************************
 * Copyright (c) 2015 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.impl;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Rectangle;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * (INTERNAL) Calculates the minimum bounding box given a bunch of rectangles (ranges).  It's a temporary object and not
 * thread-safe; throw it away when done.
 * For a cartesian space, the calculations are trivial but it is not for geodetic.  For
 * geodetic, it must maintain an ordered set of disjoint ranges as each range is provided.
 */
public class BBoxCalculator {
  
  private final SpatialContext ctx;

  private double minY = Double.POSITIVE_INFINITY;
  private double maxY = Double.NEGATIVE_INFINITY;

  private double minX = Double.POSITIVE_INFINITY;
  private double maxX = Double.NEGATIVE_INFINITY;

  /** Sorted list of <em>disjoint</em> X ranges keyed by maxX and with minX stored as the "value". */
  private TreeMap<Double, Double> ranges; // maxX -> minX

  // note: The use of a TreeMap of Double objects is a bit heavy for the points-only use-case.  In such a case,
  //  we could instead maintain an array of longitudes we just add onto during expandXRange(). A simplified version
  //  of the processRanges() method could be used that initially sorts and then proceeds in a similar but simplified
  //  fashion.

  public BBoxCalculator(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public void expandRange(Rectangle rect) {
    expandRange(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY());
  }

  public void expandRange(final double minX, final double maxX, double minY, double maxY) {
    this.minY = Math.min(this.minY, minY);
    this.maxY = Math.max(this.maxY, maxY);

    expandXRange(minX, maxX);
  }//expandRange

  public void expandXRange(double minX, double maxX) {
    if (!ctx.isGeo()) {
      this.minX = Math.min(this.minX, minX);
      this.maxX = Math.max(this.maxX, maxX);
      return;
    }

    if (doesXWorldWrap())
      return;

    if (ranges == null) {
      ranges = new TreeMap<>();
      ranges.put(maxX, minX);
      return;
    }
    assert !ranges.isEmpty();
    //now the hard part!

    //Get an iterator starting from the first entry that either contains minX or it's to the right of minX
    Iterator<Map.Entry<Double, Double>> entryIter = ranges.tailMap(minX, true/*inclusive*/).entrySet().iterator();
    if (!entryIter.hasNext()) {
      entryIter = ranges.entrySet().iterator();//wrapped across dateline
    }
    Map.Entry<Double, Double> entry = entryIter.next();

    Double entryMin = entry.getValue();
    Double entryMax = entry.getKey();

    //See if entry contains maxX
    if (rangeContains(entryMin, entryMax, maxX)) {
      // Easy: either minX is also within this entry in which case nothing to do, or it's below in which case
      // we just need to update the minX of this entry.

      //See if entry contains minX
      if (rangeContains(entryMin, entryMax, minX)) {

        // This entry & the new range together might wrap the world.
        if ( (minX != entryMin || maxX != entryMax) //ranges not equal
            && rangeContains(minX, maxX, entryMin) && rangeContains(minX, maxX, entryMax)) {
          this.minX = -180;
          this.maxX = +180;
          ranges = null;
        }
        // Done; nothing to do.
      } else {
        //Done: Update entry's start to be minX
        // note:  TreeMap's Map.Entry doesn't support setting the value :-(  So we remove & add the entry.
        entryIter.remove();
        ranges.put(entryMax, minX);
      }

    } else {//entry does NOT contain maxX:

      // We're going to insert an entry.  Determine it's min & max.  While finding the max, we'll delete entries
      //  that overlap with the new entry.

      // newMinX is basically the lower of minX & entryMin
      final Double newMinX  = rangeContains(entryMin, entryMax, minX) ? entryMin : minX;

      Double newMaxX = maxX;
      //Loop through entries (starting with current) to see if we should remove it.  At the last one, update newMaxX.
      while (rangeContains(newMinX, newMaxX, entryMin)) {
        entryIter.remove();//remove entry!
        if (!rangeContains(minX, maxX, entryMax)) {
          newMaxX = entryMax;//adjust newMaxX and stop.
          break;
        }
        // get new entry:
        if (!entryIter.hasNext()) {
          if (ranges.isEmpty()) {
            break;
          }
          //wrap around (can only happen once)
          entryIter = ranges.entrySet().iterator();
        }
        entry = entryIter.next();
        entryMin = entry.getValue();
        entryMax = entry.getKey();
      }

      //Add entry
      ranges.put(newMaxX, newMinX);
    }
  }

  private void processRanges() {
    if (ranges.size() == 1) { // an optimization
      Map.Entry<Double, Double> rangeEntry = ranges.firstEntry();
      minX = rangeEntry.getValue();
      maxX = rangeEntry.getKey();
    } else {
      // Find the biggest gap. Whenever we do, update minX & maxX for the rect opposite of the gap.
      Map.Entry<Double, Double> prevRange = ranges.lastEntry();
      double biggestGap = 0;
      double possibleRemainingGap = 360;  //calculating this enables us to exit early; often on the first lap!
      for (Map.Entry<Double, Double> range : ranges.entrySet()) {
        // calc width of this range and the gap before it.
        double widthPlusGap = range.getKey() - prevRange.getKey();// this max - last max
        if (widthPlusGap < 0) {
          widthPlusGap += 360;
        }
        double gap = range.getValue() - prevRange.getKey(); // this min - last max
        if (gap < 0) {
          gap += 360;
        }
        // reduce possibleRemainingGap by this range width and trailing gap.
        possibleRemainingGap -= widthPlusGap;
        if (gap > biggestGap) {
          biggestGap = gap;
          minX = range.getValue();
          maxX = prevRange.getKey();
          if (possibleRemainingGap <= biggestGap) {
            break;// no point in continuing
          }
        }
        prevRange = range;
      }
    }
    // Null out the ranges to signify we processed them
    ranges = null;
  }

  private static boolean rangeContains(double minX, double maxX, double x) {
    if (minX <= maxX)
      return x >= minX && x <= maxX;
    else
      return x >= minX || x <= maxX;
  }

  public boolean doesXWorldWrap() {
    assert ctx.isGeo();
    //note: not dependent on "ranges", since once we expand to world bounds then ranges is null'ed out
    return minX == -180 && maxX == 180;
  }

  public Rectangle getBoundary() {
    return ctx.makeRectangle(getMinX(), getMaxX(), getMinY(), getMaxY());
  }

  public double getMinX() {
    if (ranges != null) {
      processRanges();
    }
    return minX;
  }

  public double getMaxX() {
    if (ranges != null) {
      processRanges();
    }
    return maxX;
  }

  public double getMinY() {
    return minY;
  }

  public double getMaxY() {
    return maxY;
  }
}
