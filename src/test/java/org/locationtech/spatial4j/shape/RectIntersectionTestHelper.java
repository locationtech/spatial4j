/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import org.locationtech.spatial4j.TestLog;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.impl.InfBufLine;
import org.locationtech.spatial4j.shape.impl.PointImpl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;
import static org.locationtech.spatial4j.shape.SpatialRelation.DISJOINT;

public abstract class RectIntersectionTestHelper<S extends Shape> extends RandomizedShapeTest {

  public RectIntersectionTestHelper(SpatialContext ctx) {
    super(ctx);
  }

  /** Override to return true if generateRandomShape is essentially a Rectangle. */
  protected boolean isRandomShapeRectangular() {
    return false;
  }

  protected abstract S generateRandomShape(Point nearP);

  /** shape has no area; return a point in it */
  protected abstract Point randomPointInEmptyShape(S shape);

  // Minimum distribution of relationships

  // Each shape has different characteristics, so we don't expect (for instance) shapes that
  // are likely to be long and thin to contain as many rectangles as those that
  // short and fat.

  protected int getContainsMinimum(int laps) {
    return laps/1000;
  }

  protected int getIntersectsMinimum(int laps) {
    return laps/1000;
  }

  protected int getWithinMinimum(int laps) {
    return laps/1000;
  }

  protected int getDisjointMinimum(int laps) {
    return laps/1000;
  }

  protected int getBoundingMinimum(int laps) {
    return laps/1000;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Point randomPointInOrNull(Shape shape) {
    if (!shape.hasArea()) {
      final Point pt = randomPointInEmptyShape((S) shape);
      assert shape.relate(pt).intersects() : "faulty randomPointInEmptyShape";
      return pt;
    }
    return super.randomPointInOrNull(shape);
  }

  public void testRelateWithRectangle() {
    //counters for the different intersection cases
    int i_C = 0, i_I = 0, i_W = 0, i_D = 0, i_bboxD = 0;
    int laps = 0;
    final int MINLAPS = scaledRandomIntBetween(20000, 200000);
    while(i_C < getContainsMinimum(MINLAPS) || i_I < getIntersectsMinimum(MINLAPS) || i_W < getWithinMinimum(MINLAPS)
            || (!isRandomShapeRectangular() && i_D < getDisjointMinimum(MINLAPS)) || i_bboxD < getBoundingMinimum(MINLAPS)) {
      laps++;

      TestLog.clear();

      if (laps > MINLAPS) {
        fail("Did not find enough contains/within/intersection/disjoint/bounds cases in a reasonable number" +
                " of random attempts. CWIDbD: " +
                i_C + "("+getContainsMinimum(MINLAPS)+")," +
                i_W + "("+getWithinMinimum(MINLAPS)+")," +
                i_I + "("+getIntersectsMinimum(MINLAPS)+")," +
                i_D + "("+getDisjointMinimum(MINLAPS)+")," +
                i_bboxD + "("+getBoundingMinimum(MINLAPS)+")"
                + "  Laps exceeded " + MINLAPS);
      }

      Point nearP = randomPointIn(ctx.getWorldBounds());

      S s = generateRandomShape(nearP);

      Rectangle r = randomRectangle(s.getBoundingBox().getCenter());

      SpatialRelation ic = s.relate(r);

      TestLog.log("S-R Rel: {}, Shape {}, Rectangle {}", ic, s, r);

      if (ic != DISJOINT) {
        assertTrue("if not disjoint then the shape's bbox shouldn't be disjoint",
                s.getBoundingBox().relate(r).intersects());
      }

      try {
        int MAX_TRIES = scaledRandomIntBetween(10, 100);
        switch (ic) {
          case CONTAINS:
            i_C++;
            for (int j = 0; j < MAX_TRIES; j++) {
              Point p = randomPointIn(r);
              assertRelation(null, CONTAINS, s, p);
            }
            break;

          case WITHIN:
            i_W++;
            for (int j = 0; j < MAX_TRIES; j++) {
              Point p = randomPointInOrNull(s);
              if (p == null) {//couldn't find a random point in shape
                break;
              }
              assertRelation(null, CONTAINS, r, p);
            }
            break;

          case DISJOINT:
            if (!s.getBoundingBox().relate(r).intersects()) {//bboxes are disjoint
              i_bboxD++;
              if (i_bboxD >= getBoundingMinimum(MINLAPS))
                break;
            } else {
              i_D++;
            }
            for (int j = 0; j < MAX_TRIES; j++) {
              Point p = randomPointIn(r);
              assertRelation(null, DISJOINT, s, p);
            }
            break;

          case INTERSECTS:
            i_I++;
            SpatialRelation pointR = null;//set once
            Rectangle randomPointSpace = null;
            MAX_TRIES = 1000;//give many attempts
            for (int j = 0; j < MAX_TRIES; j++) {
              Point p;
              if (j < 4) {
                p = new PointImpl(0, 0, ctx);
                InfBufLine.cornerByQuadrant(r, j + 1, p);
              } else {
                if (randomPointSpace == null) {
                  if (pointR == DISJOINT) {
                    randomPointSpace = intersectRects(r,s.getBoundingBox());
                  } else {//CONTAINS
                    randomPointSpace = r;
                  }
                }
                p = randomPointIn(randomPointSpace);
              }
              SpatialRelation pointRNew = s.relate(p);
              if (pointR == null) {
                pointR = pointRNew;
              } else if (pointR != pointRNew) {
                break;
              }
            }

            break;

          default: fail(""+ic);
        } // switch
      } catch (AssertionError e) {
        onAssertFail(e, s, r, ic);
      }

    } // while loop

    System.out.println("Laps: "+laps + " CWIDbD: "+i_C+","+i_W+","+i_I+","+i_D+","+i_bboxD);
  }

  protected void onAssertFail(AssertionError e, S s, Rectangle r, SpatialRelation ic) {
    throw e;
  }

  private Rectangle intersectRects(Rectangle r1, Rectangle r2) {
    assert r1.relate(r2).intersects();
    final double minX, maxX;
    if (r1.relateXRange(r2.getMinX(),r2.getMinX()).intersects()) {
      minX = r2.getMinX();
    } else {
      minX = r1.getMinX();
    }
    if (r1.relateXRange(r2.getMaxX(),r2.getMaxX()).intersects()) {
      maxX = r2.getMaxX();
    } else {
      maxX = r1.getMaxX();
    }
    final double minY, maxY;
    if (r1.relateYRange(r2.getMinY(),r2.getMinY()).intersects()) {
      minY = r2.getMinY();
    } else {
      minY = r1.getMinY();
    }
    if (r1.relateYRange(r2.getMaxY(),r2.getMaxY()).intersects()) {
      maxY = r2.getMaxY();
    } else {
      maxY = r1.getMaxY();
    }
    return ctx.makeRectangle(minX, maxX, minY, maxY);
  }

}
