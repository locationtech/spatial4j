/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.impl.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.locationtech.spatial4j.shape.SpatialRelation.CONTAINS;
import static org.locationtech.spatial4j.shape.SpatialRelation.DISJOINT;
import static org.locationtech.spatial4j.shape.SpatialRelation.INTERSECTS;


public class TestShapes2D extends AbstractTestShapes {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    final Rectangle WB = new RectangleImpl(-2000, 2000, -300, 300, null);//whatever

    List<Object[]> ctxs = new ArrayList<Object[]>();
    ctxs.add($(new SpatialContextFactory() {{geo = false; worldBounds = WB;}}.newSpatialContext()));
    ctxs.add($(new JtsSpatialContextFactory() {{geo = false; worldBounds = WB;}}.newSpatialContext()));
    return ctxs;
  }

  public TestShapes2D(SpatialContext ctx) {
    super(ctx);
  }

  @Test
  public void testSimplePoint() {
    try { ctx.makePoint(2001,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makePoint(0, -301); fail(); } catch (InvalidShapeException e) {}

    Point pt = ctx.makePoint(0,0);
    String msg = pt.toString();

    //test equals & hashcode
    Point pt2 = ctx.makePoint(0,0);
    assertEquals(msg, pt, pt2);
    assertEquals(msg, pt.hashCode(), pt2.hashCode());

    assertFalse(msg,pt.hasArea());
    assertEquals(msg,pt.getCenter(),pt);
    Rectangle bbox = pt.getBoundingBox();
    assertFalse(msg,bbox.hasArea());
    assertEquals(msg,pt,bbox.getCenter());

    assertRelation(msg, CONTAINS, pt, pt2);
    assertRelation(msg, DISJOINT, pt, ctx.makePoint(0, 1));
    assertRelation(msg, DISJOINT, pt, ctx.makePoint(1, 0));
    assertRelation(msg, DISJOINT, pt, ctx.makePoint(1, 1));

    pt.reset(1, 2);
    assertEquals(ctx.makePoint(1, 2), pt);

    assertEquals(ctx.makeCircle(pt, 3), pt.getBuffered(3, ctx));

    testEmptiness(ctx.makePoint(Double.NaN, Double.NaN));
  }

  @Test
  public void testSimpleRectangle() {
    double v = 2001 * (randomBoolean() ? -1 : 1);
    try { ctx.makeRectangle(v,0,0,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,v,0,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,v,0); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,0,v); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(0,0,10,-10); fail(); } catch (InvalidShapeException e) {}
    try { ctx.makeRectangle(10,-10,0,0); fail(); } catch (InvalidShapeException e) {}

    double[] minXs = new double[]{-1000,-360,-180,-20,0,20,180,1000};
    for (double minX : minXs) {
      double[] widths = new double[]{0,10,180,360,400};
      for (double width : widths) {
        testRectangle(minX, width, 0, 0);
        testRectangle(minX, width, -10, 10);
        testRectangle(minX, width, 5, 10);
      }
    }

    Rectangle r = ctx.makeRectangle(0, 0, 0, 0);
    r.reset(1, 2, 3, 4);
    assertEquals(ctx.makeRectangle(1, 2, 3, 4), r);

    testRectIntersect();

    if (!ctx.isGeo())
      assertEquals(ctx.makeRectangle(0.9, 2.1, 2.9, 4.1), ctx.makeRectangle(1, 2, 3, 4).getBuffered(0.1, ctx));

    testEmptiness(ctx.makeRectangle(Double.NaN, Double.NaN, Double.NaN, Double.NaN));
  }

  @Test
  public void testSimpleCircle() {
    double[] theXs = new double[]{-10,0,10};
    for (double x : theXs) {
      double[] theYs = new double[]{-20,0,20};
      for (double y : theYs) {
        testCircle(x, y, 0);
        testCircle(x, y, 5);
      }
    }

    testCircleReset(ctx);

    //INTERSECTION:
    //Start with some static tests that have shown to cause failures at some point:
    assertEquals("getX not getY",INTERSECTS,ctx.makeCircle(107,-81,147).relate(ctx.makeRectangle(92, 121, -89, 74)));

    testCircleIntersect();

    assertEquals(ctx.makeCircle(1, 2, 10), ctx.makeCircle(1, 2, 6).getBuffered(4, ctx));

    testEmptiness(ctx.makeCircle(Double.NaN, Double.NaN, randomBoolean() ? 0 : Double.NaN));
  }

  static void testCircleReset(SpatialContext ctx) {
    Circle c = ctx.makeCircle(3, 4, 5);
    Circle c2 = ctx.makeCircle(5, 6, 7);
    c2.reset(3,4,5);// to c1
    assertEquals(c, c2);
    assertEquals(c.getBoundingBox(), c2.getBoundingBox());
  }

  @Test
  public void testLineString() {
    //see BufferedLineStringTest & BufferedLineTest for more
    Shape shape = ctx.getShapeFactory().lineString().buffer(randomInt(3)).build();
    testEmptiness(shape);
  }

  /** We have this test here but we'll add geo shapes as needed. */
  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    checkShapesImplementEquals( new Class[] {
            PointImpl.class,
            CircleImpl.class,
            //GeoCircle.class  no: its fields are caches, not part of its identity
            RectangleImpl.class,
            ShapeCollection.class,
            BufferedLineString.class,
            BufferedLine.class
    });
  }

}
