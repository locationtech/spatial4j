/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.shape.impl.BufferedLineString;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BufferedLineStringTest extends RandomizedTest {

  private final SpatialContext ctx = new SpatialContextFactory()
    {{geo = false; worldBounds = new RectangleImpl(-100, 100, -50, 50, null);}}.newSpatialContext();


  @Test
  public void testRectIntersect() {
    new RectIntersectionTestHelper<BufferedLineString>(ctx) {

      @Override
      protected BufferedLineString generateRandomShape(Point nearP) {
        Rectangle nearR = randomRectangle(nearP);
        int numPoints = 2 + randomInt(3);//2-5 points

        ArrayList<Point> points = new ArrayList<Point>(numPoints);
        while (points.size() < numPoints) {
          points.add(randomPointIn(nearR));
        }
        double maxBuf = Math.max(nearR.getWidth(), nearR.getHeight());
        double buf = Math.abs(randomGaussian()) * maxBuf / 4;
        buf = randomInt((int) divisible(buf));
        return new BufferedLineString(points, buf, ctx);
      }

      protected Point randomPointInEmptyShape(BufferedLineString shape) {
        List<Point> points = shape.getPoints();
        return points.get(randomInt(points.size() - 1));
      }
    }.testRelateWithRectangle();
  }

}
