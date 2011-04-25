package org.apache.lucene.spatial.test.strategy;

import java.io.IOException;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.geohash.GeohashStrategy;
import org.apache.lucene.spatial.strategy.geohash.GridReferenceSystem;
import org.apache.lucene.spatial.test.SpatialMatchConcerns;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;


public abstract class BaseGeohashStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  protected abstract SpatialContext getSpatialContext();


  @Test
  public void testGeohashStrategy() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    int maxLength = GridReferenceSystem.getMaxPrecision();
    GridReferenceSystem grs = new GridReferenceSystem(
        getSpatialContext(), maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    // SimpleIO
    executeQueries( s, grs.shapeIO, finfo,
        SpatialMatchConcerns.FILTER,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
