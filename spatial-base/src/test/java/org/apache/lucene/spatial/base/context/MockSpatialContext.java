package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Circle;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;


public class MockSpatialContext extends SpatialContext {

  @Override
  public DistanceUnits getUnits() {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Shape readShape(String value) throws InvalidShapeException {
    return null;
  }

  @Override
  public String toString(Shape shape) {
    return null;
  }

  @Override
  public Point makePoint(double x, double y) {
    return null;
  }

  @Override
  public Rectangle makeRect(double minX, double maxX, double minY, double maxY) {
    return null;
  }

  @Override
  public Circle makeCircle(double x, double y, double distance) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public DistanceCalculator getDistanceCalculator(Class<? extends DistanceCalculator> clazz) {
    return null;
  }
}
