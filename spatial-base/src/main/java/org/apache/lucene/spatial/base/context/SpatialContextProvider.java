package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.jts.JtsSpatialContext;

public class SpatialContextProvider {

  private SpatialContextProvider() {
  }

  private static SpatialContext instance = null;

  public static synchronized SpatialContext getShapeIO() {
    if( instance == null ) {
      instance = new JtsSpatialContext();
    }
    return instance;
  }
}
