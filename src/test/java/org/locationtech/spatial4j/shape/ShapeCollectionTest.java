/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import org.locationtech.spatial4j.TestLog;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;

public class ShapeCollectionTest extends RandomizedShapeTest {

  public static final String WORLD180 = getLonRangeString(SpatialContext.GEO.getWorldBounds());

  protected static String getLonRangeString(Rectangle bbox) {
    return bbox.getMinX()+" "+bbox.getMaxX();
  }

  @Rule
  public final TestLog testLog = TestLog.instance;

  @Test
  public void testBbox() {
    validateWorld(-180, 180, -180, 180);
    validateWorld(-180, 0, 0, +180);
    validateWorld(-90, +90, +90, -90);
  }

  @Test
  public void testBboxNotWorldWrap() {
    ctx = SpatialContext.GEO;
    //doesn't contain 102, thus shouldn't world-wrap
    Rectangle r1 = ctx.makeRectangle(-92, 90, -10, 10);
    Rectangle r2 = ctx.makeRectangle(130, 172, -10, 10);
    Rectangle r3 = ctx.makeRectangle(172, -60, -10, 10);
    ShapeCollection<Rectangle> s = new ShapeCollection<>(Arrays.asList(r1, r2, r3), ctx);
    assertEquals("130.0 90.0", getLonRangeString(s.getBoundingBox()));
    // note: BBoxCalculatorTest thoroughly tests the longitude range
  }


  private void validateWorld(double r1MinX, double r1MaxX, double r2MinX, double r2MaxX) {
    ctx = SpatialContext.GEO;
    Rectangle r1 = ctx.makeRectangle(r1MinX, r1MaxX, -10, 10);
    Rectangle r2 = ctx.makeRectangle(r2MinX, r2MaxX, -10, 10);

    ShapeCollection<Rectangle> s = new ShapeCollection<>(Arrays.asList(r1, r2), ctx);
    assertEquals(WORLD180, getLonRangeString(s.getBoundingBox()));

    //flip r1, r2 order
    s = new ShapeCollection<>(Arrays.asList(r2, r1), ctx);
    assertEquals(WORLD180, getLonRangeString(s.getBoundingBox()));
  }

  @Test
  public void testRectIntersect() {
    SpatialContext ctx = new SpatialContextFactory()
      {{geo = false; worldBounds = new RectangleImpl(-100, 100, -50, 50, null);}}.newSpatialContext();

    new ShapeCollectionRectIntersectionTestHelper(ctx).testRelateWithRectangle();
  }

  @Test
  public void testGeoRectIntersect() {
    ctx = SpatialContext.GEO;
    new ShapeCollectionRectIntersectionTestHelper(ctx).testRelateWithRectangle();
  }

  private class ShapeCollectionRectIntersectionTestHelper extends RectIntersectionTestHelper<ShapeCollection> {

    private ShapeCollectionRectIntersectionTestHelper(SpatialContext ctx) {
      super(ctx);
    }

    @Override
    protected ShapeCollection generateRandomShape(Point nearP) {
      testLog.log("Break on nearP.toString(): {}", nearP);
      List<Rectangle> shapes = new ArrayList<>();
      int count = randomIntBetween(1,4);
      for(int i = 0; i < count; i++) {
        //1st 2 are near nearP, the others are anywhere
        shapes.add(randomRectangle( i < 2 ? nearP : null));
      }
      ShapeCollection<Rectangle> shapeCollection = new ShapeCollection<>(shapes, ctx);

      //test shapeCollection.getBoundingBox();
      Rectangle msBbox = shapeCollection.getBoundingBox();
      if (shapes.size() == 1) {
        assertEquals(shapes.get(0), msBbox.getBoundingBox());
      } else {
        for (Rectangle shape : shapes) {
          assertRelation("bbox contains shape", CONTAINS, msBbox, shape);
        }
        if (ctx.isGeo() && msBbox.getMinX() == -180 && msBbox.getMaxX() == 180) {
          int lonTest = randomIntBetween(-180, 180);
          boolean valid = false;
          for (Rectangle shape : shapes) {
            if (shape.relateXRange(lonTest, lonTest).intersects()) {
              valid = true;
              break;
            }
          }
          if (!valid)
            fail("ShapeCollection bbox world-wrap doesn't contain "+lonTest+" for shapes: "+shapes);
        }
      }
      return shapeCollection;
    }

    protected Point randomPointInEmptyShape(ShapeCollection shape) {
      Rectangle r = (Rectangle) shape.getShapes().get(0);
      return randomPointIn(r);
    }
  }
}
