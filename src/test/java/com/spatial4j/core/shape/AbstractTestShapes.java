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

import com.spatial4j.core.TestLog;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Rule;
import org.junit.Test;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;


/**
 * Some basic tests that should work with various {@link SpatialContext}
 * configurations.  Subclasses add more.
 */
public abstract class AbstractTestShapes extends RandomizedShapeTest {

  public AbstractTestShapes(SpatialContext ctx) {
    super(ctx);
  }

  @Rule
  public final TestLog testLog = TestLog.instance;

  protected void testRectangle(double minX, double width, double minY, double height) {
    double maxX = minX + width;
    double maxY = minY + height;
    minX = normX(minX);
    maxX = normX(maxX);

    Rectangle r = ctx.makeRectangle(minX, maxX, minY, maxY);
    //test equals & hashcode of duplicate
    Rectangle r2 = ctx.makeRectangle(minX, maxX, minY, maxY);
    assertEquals(r,r2);
    assertEquals(r.hashCode(),r2.hashCode());

    String msg = r.toString();

    assertEquals(msg, width != 0 && height != 0, r.hasArea());
    assertEquals(msg, width != 0 && height != 0, r.getArea(ctx) > 0);
    if (ctx.isGeo() && r.getWidth() == 360 && r.getHeight() == 180) {
      //whole globe
      double earthRadius = DistanceUtils.toDegrees(1);
      assertEquals(4*Math.PI * earthRadius * earthRadius, r.getArea(ctx), 1.0);//1km err
    }

    assertEqualsRatio(msg, height, r.getHeight());
    assertEqualsRatio(msg, width, r.getWidth());
    Point center = r.getCenter();
    msg += " ctr:"+center;
    //System.out.println(msg);
    assertRelation(msg, CONTAINS, r, center);

    DistanceCalculator dc = ctx.getDistCalc();
    double dUR = dc.distance(center, r.getMaxX(), r.getMaxY());
    double dLR = dc.distance(center, r.getMaxX(), r.getMinY());
    double dUL = dc.distance(center, r.getMinX(), r.getMaxY());
    double dLL = dc.distance(center, r.getMinX(), r.getMinY());

    assertEquals(msg,width != 0 || height != 0, dUR != 0);
    if (dUR != 0)
      assertTrue(dUR > 0 && dLL > 0);
    assertEqualsRatio(msg, dUR, dUL);
    assertEqualsRatio(msg, dLR, dLL);
    if (!ctx.isGeo() || center.getY() == 0)
      assertEqualsRatio(msg, dUR, dLL);
  }

  protected void testRectIntersect() {
    //This test loops past the dateline for some variables but the makeNormRect()
    // method ensures the rect is valid.
    final double INCR = 45;
    final double Y = 20;
    for(double left = -180; left < 180; left += INCR) {
      for(double right = left; right - left <= 360; right += INCR) {
        Rectangle r = makeNormRect(left, right, -Y, Y);

        //test contains (which also tests within)
        for(double left2 = left; left2 <= right; left2 += INCR) {
          for(double right2 = left2; right2 <= right; right2 += INCR) {
            Rectangle r2 = makeNormRect(left2, right2, -Y, Y);
            assertRelation(null, SpatialRelation.CONTAINS, r, r2);

            //test point contains
            assertRelation(null, SpatialRelation.CONTAINS, r, r2.getCenter());
          }
        }

        //test disjoint
        for(double left2 = right+INCR; left2 - left < 360; left2 += INCR) {
          //test point disjoint
          assertRelation(null, SpatialRelation.DISJOINT, r, ctx.makePoint(
                  normX(left2), randomIntBetween(-90, 90)));

          for(double right2 = left2; right2 - left < 360; right2 += INCR) {
            Rectangle r2 = makeNormRect(left2, right2, -Y, Y);
            assertRelation(null, SpatialRelation.DISJOINT, r, r2);
          }
        }
        //test intersect
        for(double left2 = left+INCR; left2 <= right; left2 += INCR) {
          for(double right2 = right+INCR; right2 - left < 360; right2 += INCR) {
            Rectangle r2 = makeNormRect(left2, right2, -Y, Y);
            assertRelation(null, SpatialRelation.INTERSECTS, r, r2);
          }
        }

      }
    }
  }

  protected void testCircle(double x, double y, double dist) {
    Circle c = ctx.makeCircle(x, y, dist);
    String msg = c.toString();
    final Circle c2 = ctx.makeCircle(ctx.makePoint(x, y), dist);
    assertEquals(c, c2);
    assertEquals(c.hashCode(),c2.hashCode());

    assertEquals(msg, dist > 0, c.hasArea());
    double area = c.getArea(ctx);
    assertTrue(msg, c.hasArea() == (area > 0.0));
    final Rectangle bbox = c.getBoundingBox();
    assertEquals(msg, dist > 0, bbox.getArea(ctx) > 0);
    assertTrue(msg, area <= bbox.getArea(ctx));
    if (!ctx.isGeo()) {
      //if not geo then units of dist == units of x,y
      assertEqualsRatio(msg, bbox.getHeight(), dist * 2);
      assertEqualsRatio(msg, bbox.getWidth(), dist * 2);
    }

    assertRelation(msg, CONTAINS, c, c.getCenter());
    assertRelation(msg, CONTAINS, bbox, c);
  }

  protected void testCircleIntersect() {
    new RectIntersectionTestHelper<Circle>(ctx) {
      @Override
      protected Circle generateRandomShape(Point nearP) {
        double cX = randomIntBetweenDivisible(-180, 179);
        double cY = randomIntBetweenDivisible(-90, 90);
        double cR_dist = randomIntBetweenDivisible(0, 180);
        return ctx.makeCircle(cX, cY, cR_dist);
      }

      @Override
      protected Point randomPointInEmptyShape(Circle shape) {
        return shape.getCenter();
      }

      @Override
      protected void onAssertFail(AssertionError e, Circle s, Rectangle r, SpatialRelation ic) {
        //Check if the circle's edge appears to coincide with the shape.
        final double radius = s.getRadius();
        if (radius == 180)
          throw e;//if this happens, then probably a bug
        if (radius == 0) {
          Point p = s.getCenter();
          //if touches a side then don't throw
          if (p.getX() == r.getMinX() || p.getX() == r.getMaxX()
            || p.getY() == r.getMinY() || p.getY() == r.getMaxY())
            return;
          throw e;
        }
        final double eps = 0.0000001;
        s.reset(s.getCenter().getX(), s.getCenter().getY(), radius - eps);
        SpatialRelation rel1 = s.relate(r);
        s.reset(s.getCenter().getX(), s.getCenter().getY(), radius + eps);
        SpatialRelation rel2 = s.relate(r);
        if (rel1 == rel2)
          throw e;
        s.reset(s.getCenter().getX(), s.getCenter().getY(), radius);//reset
        System.out.println("Seed "+getContext().getRunnerSeedAsString()+": Hid assertion due to ambiguous edge touch: "+s+" "+r);
      }
    }.testRelateWithRectangle();
  }

  @Test
  public void testMakeRect() {
    //test rectangle constructor
    assertEquals(new RectangleImpl(1,3,2,4, ctx),
        new RectangleImpl(new PointImpl(1,2, ctx),new PointImpl(3,4, ctx), ctx));

    //test ctx.makeRect
    assertEquals(ctx.makeRectangle(1, 3, 2, 4),
        ctx.makeRectangle(ctx.makePoint(1, 2), ctx.makePoint(3, 4)));
  }

  protected void testEmptiness(Shape emptyShape) {
    assertTrue(emptyShape.isEmpty());
    Point emptyPt = emptyShape.getCenter();
    assertTrue(emptyPt.isEmpty());
    Rectangle emptyRect = emptyShape.getBoundingBox();
    assertTrue(emptyRect.isEmpty());
    assertEquals(emptyRect, emptyShape.getBoundingBox());
    assertEquals(emptyPt, emptyShape.getCenter());
    assertRelation("EMPTY", DISJOINT, emptyShape, emptyPt);
    assertRelation("EMPTY", DISJOINT, emptyShape, randomPoint());
    assertRelation("EMPTY", DISJOINT, emptyShape, emptyRect);
    assertRelation("EMPTY", DISJOINT, emptyShape, randomRectangle(10));
    assertTrue(emptyShape.getBuffered(randomInt(4), ctx).isEmpty());
  }
}
