package com.spatial4j.core.shape.impl;


import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.*;

import java.util.List;

public class GeoBufferedLineString implements Shape {
  @Override
  public SpatialRelation relate(Shape other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rectangle getBoundingBox() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasArea() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getArea(SpatialContext ctx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point getCenter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Shape getBuffered(double distance, SpatialContext ctx) {
    throw new UnsupportedOperationException();
  }

  public Shape getBuffered(SpatialContext ctx, double distance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
    }

    @Override
    public Point getB() {
      throw new UnsupportedOperationException();
    }
  }
}
