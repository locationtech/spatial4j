package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public abstract class AbstractDistanceCalculator implements DistanceCalculator {
  //@Override
  public double calculate(Point from, Shape shape) {
    if (Point.class.isInstance(shape)) {
      return calculate(from, (Point)shape);
    }
    throw new UnsupportedOperationException( "Distance to shape is not yet supported" );
  }

  @Override
  public double calculate(Point from, Point point) {
    return calculate(from,point.getX(),point.getY());
  }
}
