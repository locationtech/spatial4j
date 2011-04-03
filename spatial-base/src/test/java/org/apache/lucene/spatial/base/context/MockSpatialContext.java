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

  public Shape readShape(String value) throws InvalidShapeException {
    return null;
  }

  public String toString(Shape shape) {
    return null;
  }

  public Point makePoint(double x, double y) {
    return null;
  }

  public BBox makeBBox(double minX, double maxX, double minY, double maxY) {
    return null;
  }

  public DistanceCalculator getDistanceCalculator(Class<? extends DistanceCalculator> clazz) {
    return null;
  }
}
