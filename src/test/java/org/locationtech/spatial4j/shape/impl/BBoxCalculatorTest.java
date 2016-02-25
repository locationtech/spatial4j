/*******************************************************************************
 * Copyright (c) 2015 David Smiley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.impl;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.RandomizedShapeTest;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BBoxCalculatorTest extends RandomizedShapeTest {

  public BBoxCalculatorTest() {
    super(SpatialContext.GEO);
  }

  // note: testing latitude would be so simple that's effectively the same code as the code to be tested. So I don't.


  @Test @Repeat(iterations = 100)
  public void testGeoLongitude() {
    BBoxCalculator calc = new BBoxCalculator(ctx);
    final int numShapes = randomIntBetween(1, 4);//inclusive
    List<Rectangle> rects = new ArrayList<>(numShapes);
    for (int i = 0; i < numShapes; i++) {
      Rectangle rect = randomRectangle(30);// divisible by
      rects.add(rect);
      calc.expandRange(rect);
    }
    Rectangle boundary = calc.getBoundary();
    if (numShapes == 1) {
      assertEquals(rects.get(0), boundary);
      return;
    }

    // If the boundary is the world-bounds, check that it's right.
    if (boundary.getMinX() == -180 && boundary.getMaxX() == 180) {
      // each longitude should be present in at least one shape:
      for (int lon = -180; lon <= +180; lon++) {
        assertTrue(atLeastOneRectHasLon(rects, lon));
      }
      return;
    }

    // Test that it contains all shapes:
    for (Rectangle rect : rects) {
      assertRelation(SpatialRelation.CONTAINS, boundary, rect);
    }

    // Test that the left & right are boundaries:
    assertTrue(atLeastOneRectHasLon(rects, boundary.getMinX()));
    assertFalse(atLeastOneRectHasLon(rects, normX(boundary.getMinX() - 0.5)));

    assertTrue(atLeastOneRectHasLon(rects, boundary.getMaxX()));
    assertFalse(atLeastOneRectHasLon(rects, normX(boundary.getMaxX() + 0.5)));

    // Test that this is the smallest enclosing boundary by ensuring the gap (opposite the bbox) is
    //  the largest:
    if (boundary.getWidth() > 180) { // conversely if wider than 180 then no wider gap is possible
      double biggerGap = 360.0 - boundary.getWidth() + 0.5;
      for (Rectangle rect : rects) {
        // try to see if a bigger gap could lie to the right of this rect
        double gapRectLeft = rect.getMaxX() + 0.25;
        double gapRectRight = gapRectLeft + biggerGap;
        Rectangle testGap = makeNormRect(gapRectLeft, gapRectRight, -90, 90);
        boolean fits = true;
        for (Rectangle rect2 : rects) {
          if (rect2.relate(testGap).intersects()) {
            fits = false;
            break;
          }
        }
        assertFalse(fits);//should never fit because it's larger than the biggest gap
      }
    }
  }

  private boolean atLeastOneRectHasLon(List<Rectangle> rects, double lon) {
    for (Rectangle rect : rects) {
      if (rect.relateXRange(lon, lon).intersects()) {
        return true;
      }
    }
    return false;
  }

}