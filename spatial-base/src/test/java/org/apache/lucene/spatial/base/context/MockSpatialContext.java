package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * @author Chris Male
 */
public class MockSpatialContext extends SpatialContext {

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
  public BBox makeBBox(double minX, double maxX, double minY, double maxY) {
    return null;
  }

  @Override
  public DistanceCalculator getDistanceCalculator(Class<? extends DistanceCalculator> clazz) {
    return null;
  }
}
