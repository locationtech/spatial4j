package com.spatial4j.core.shape;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.AbstractDistanceCalculator;
import com.spatial4j.core.distance.DistanceCalculator;

/** Ameliorates some random tests cases in which shapes barely tough or barely not
 * touch. */
class RoundingDistCalc extends AbstractDistanceCalculator {
  DistanceCalculator delegate;

  RoundingDistCalc(DistanceCalculator delegate) {
    this.delegate = delegate;
  }

  double round(double val) {
    final double scale = Math.pow(10,10/*digits precision*/);
    return Math.round(val * scale) / scale;
  }

  @Override
  public double distance(Point from, double toX, double toY) {
    return round(delegate.distance(from, toX, toY));
  }

  @Override
  public Point pointOnBearing(Point from, double distDEG, double bearingDEG, SpatialContext ctx, Point reuse) {
    return delegate.pointOnBearing(from, distDEG, bearingDEG, ctx, reuse);
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distDEG, SpatialContext ctx, Rectangle reuse) {
    return delegate.calcBoxByDistFromPt(from, distDEG, ctx, reuse);
  }

  @Override
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distDEG, SpatialContext ctx) {
    return delegate.calcBoxByDistFromPt_yHorizAxisDEG(from, distDEG, ctx);
  }

  @Override
  public double area(Rectangle rect) {
    return delegate.area(rect);
  }

  @Override
  public double area(Circle circle) {
    return delegate.area(circle);
  }
}
