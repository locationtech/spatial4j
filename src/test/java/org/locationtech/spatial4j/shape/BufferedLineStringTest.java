/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
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

        ArrayList<Point> points = new ArrayList<>(numPoints);
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
