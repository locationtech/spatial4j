package org.apache.lucene.spatial.base;


public interface DistanceCalculator {
  public double calculate( Shape shape );
  public double calculate( Point point );
}
