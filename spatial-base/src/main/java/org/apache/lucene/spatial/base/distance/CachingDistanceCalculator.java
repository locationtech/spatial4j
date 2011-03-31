package org.apache.lucene.spatial.base.distance;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

public class CachingDistanceCalculator implements DistanceCalculator {
  private final DistanceCalculator calculator;
  private final Map<Object, Double> cache = new HashMap<Object, Double>(); // weak?

  public CachingDistanceCalculator( DistanceCalculator calculator ) {
    this.calculator = calculator;
  }

  @Override
  public double calculate(Point from, Shape shape, int key) {
    if( key < 0 ) {
      return calculator.calculate(from, shape, key);
    }
    Double v = cache.get( key );
    if( v == null ) {
      v = new Double( calculator.calculate(from, shape, key) );
      cache.put( key, v );
    }
    return v.doubleValue();
  }

  @Override
  public double calculate(Point from, Point point, int key) {
    if( key < 0 ) {
      return calculator.calculate(from, point, key);
    }
    Double v = cache.get( key );
    if( v == null ) {
      v = new Double( calculator.calculate(from, point, key) );
      cache.put( key, v );
    }
    return v.doubleValue();
  }
}
