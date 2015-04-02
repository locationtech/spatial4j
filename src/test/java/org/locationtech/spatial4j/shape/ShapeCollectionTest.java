/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Ryan McKinley - initial API and implementation
 *    David Smiley
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import org.junit.Rule;
import org.junit.Test;
import org.locationtech.spatial4j.TestLog;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.shape.impl.Range;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;

/** @author David Smiley - dsmiley@mitre.org */
public class ShapeCollectionTest extends RandomizedShapeTest {

  @Rule
  public final TestLog testLog = TestLog.instance;

  @Test
  public void testBbox() {
    validateWorld(-180, 180, -180, 180);
    validateWorld(-180, 0, 0, +180);
    validateWorld(-90, +90, +90, -90);
  }

  private void validateWorld(double r1MinX, double r1MaxX, double r2MinX, double r2MaxX) {
    ctx = SpatialContext.GEO;
    Rectangle r1 = ctx.makeRectangle(r1MinX, r1MaxX, -10, 10);
    Rectangle r2 = ctx.makeRectangle(r2MinX, r2MaxX, -10, 10);

    ShapeCollection<Rectangle> s = new ShapeCollection<Rectangle>(Arrays.asList(r1,r2), ctx);
    assertEquals(Range.LongitudeRange.WORLD_180E180W, new Range.LongitudeRange(s.getBoundingBox()));

    //flip r1, r2 order
    s = new ShapeCollection<Rectangle>(Arrays.asList(r2,r1), ctx);
    assertEquals(Range.LongitudeRange.WORLD_180E180W, new Range.LongitudeRange(s.getBoundingBox()));
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
      List<Rectangle> shapes = new ArrayList<Rectangle>();
      int count = randomIntBetween(1,4);
      for(int i = 0; i < count; i++) {
        //1st 2 are near nearP, the others are anywhere
        shapes.add(randomRectangle( i < 2 ? nearP : null));
      }
      ShapeCollection shapeCollection = new ShapeCollection<Rectangle>(shapes, ctx);

      //test shapeCollection.getBoundingBox();
      Rectangle msBbox = shapeCollection.getBoundingBox();
      if (shapes.size() == 1) {
        assertEquals(shapes.get(0), msBbox.getBoundingBox());
      } else {
        for (Rectangle shape : shapes) {
          assertRelation("bbox contains shape", CONTAINS, msBbox, shape);
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
