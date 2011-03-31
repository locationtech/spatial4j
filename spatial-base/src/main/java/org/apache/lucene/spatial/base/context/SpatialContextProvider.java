package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;

public class SpatialContextProvider {

  private SpatialContextProvider() {
  }

  private static SpatialContext instance = null;

  public static synchronized SpatialContext getShapeIO() {
    if( instance == null ) {
      // TODO... get the best one
      instance = new SimpleSpatialContext();
    }
    return instance;
  }
}
