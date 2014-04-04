package com.spatial4j.core.shape.impl;


import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.*;

import java.util.List;

public class GeoBufferedLineString implements Shape {
  @Override
  public SpatialRelation relate(Shape other) {
    return null;
  }

  @Override
  public Rectangle getBoundingBox() {
    return null;
  }

  @Override
  public boolean hasArea() {
    return false;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return 0;
  }

  @Override
  public Point getCenter() {
    return null;
  }

  @Override
  public Shape getBuffered(SpatialContext ctx, double distance) {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  static class PairSegment extends ShapeCollection<GeoBufferedLine> implements LineSegment {

    /**
     * WARNING: {@code shapes} is copied by reference.
     *
     * @param shapes Copied by reference! (make a defensive copy if caller modifies)
     * @param ctx
     */
    public PairSegment(List<GeoBufferedLine> shapes, SpatialContext ctx) {
      super(shapes, ctx);
    }

    @Override
    public Point getA() {
      return null;
    }

    @Override
    public Point getB() {
      return null;
    }
  }
}
