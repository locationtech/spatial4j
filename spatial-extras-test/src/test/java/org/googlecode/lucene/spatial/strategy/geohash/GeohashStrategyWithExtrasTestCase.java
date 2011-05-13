package org.googlecode.lucene.spatial.strategy.geohash;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.geohash.GeohashStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class GeohashStrategyWithExtrasTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  /**
   * For now, the only difference from the Simple version is that this uses JtsSpatialContext
   */
  @Test
  public void testGeohashStrategy() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    int maxLength = GeohashSpatialPrefixGrid.getMaxLevelsPossible();
    GeohashSpatialPrefixGrid grs = new GeohashSpatialPrefixGrid(
        new JtsSpatialContext(), maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    // SimpleIO
    executeQueries( s, grs.getShapeIO(), finfo,
        SpatialMatchConcern.FILTER,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
