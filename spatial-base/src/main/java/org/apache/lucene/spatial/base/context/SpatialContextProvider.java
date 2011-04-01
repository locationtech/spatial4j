package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialContextProvider {
  static final Logger log = LoggerFactory.getLogger( SpatialContextProvider.class );

  private SpatialContextProvider() {
  }

  private static SpatialContext instance = null;

  // TODO -- we really want to pass initalization info to this guy...
  @SuppressWarnings("unchecked")
  public static synchronized SpatialContext getContext() {
    if( instance == null ) {
      Class<? extends SpatialContext> clazz = null;
      String cname = System.getProperty( "SpatialContextProvider" );
      if( cname != null ) {
        try {
          clazz = (Class<? extends SpatialContext>) Class.forName( cname );
          instance = clazz.newInstance();
          return instance;
        }
        catch (Exception e) {
          System.out.println("ERROR:" + e);
          log.warn( "Using default SpatialContext", e );
        }
      }
//      if( clazz == null ) {
//        try {
//          clazz = (Class<? extends SpatialContext>)
//            Class.forName( "com.voyagergis.community.lucene.spatial.JtsSpatialContext" );
//        }
//        catch (ClassNotFoundException e) {
//          log.warn( "Using default SpatialContext", e );
//        }
//      }

      // TODO... get the best one
      instance = new SimpleSpatialContext();
    }
    return instance;
  }
}
