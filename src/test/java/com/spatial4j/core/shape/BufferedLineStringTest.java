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
