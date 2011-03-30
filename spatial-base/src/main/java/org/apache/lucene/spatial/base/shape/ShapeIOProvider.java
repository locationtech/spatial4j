package org.apache.lucene.spatial.base.shape;

import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;

public class ShapeIOProvider {

  private ShapeIOProvider() {
  }

  private static ShapeIO instance = null;

  public static synchronized ShapeIO getShapeIO() {
    if( instance == null ) {
      instance = new JtsShapeIO(DistanceUnits.KILOMETERS);
    }
    return instance;
  }
}
