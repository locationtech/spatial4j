package org.apache.lucene.spatial.base.distance;

import org.apache.lucene.spatial.base.DistanceCalculator;
import org.apache.lucene.spatial.base.Point;
import org.apache.lucene.spatial.base.Shape;


public class EuclidianDistanceCalculator implements DistanceCalculator
{
  public final double centerX = 0;
  public final double centerY = 0;

  public double calculate( Shape shape )
  {
    throw new UnsupportedOperationException( "not implemented" );
  }

  public double calculate( Point point )
  {
    return 10;
  }
}
