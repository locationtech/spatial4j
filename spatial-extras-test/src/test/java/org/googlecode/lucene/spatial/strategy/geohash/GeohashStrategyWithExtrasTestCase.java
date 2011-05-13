package org.googlecode.lucene.spatial.strategy.geohash;

import java.io.IOException;

import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.DynamicPrefixStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


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
    DynamicPrefixStrategy s = new DynamicPrefixStrategy( grs );

    // SimpleIO
    executeQueries( s, grs.getShapeIO(), finfo,
        SpatialMatchConcern.FILTER,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
