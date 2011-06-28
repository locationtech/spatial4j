package org.googlecode.lucene.spatial.strategy.prefix;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.RecursiveGridStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class RecursiveGridStrategyWithExtrasTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    int maxLength = GeohashSpatialPrefixGrid.getMaxLevelsPossible();
    this.ctx = new JtsSpatialContext();
    GeohashSpatialPrefixGrid grid = new GeohashSpatialPrefixGrid(
        ctx, maxLength );
    this.strategy = new RecursiveGridStrategy( grid );
    this.fieldInfo = new SimpleSpatialFieldInfo( "geohash" );
  }

  /**
   * For now, the only difference from the Simple version is that this uses JtsSpatialContext
   */
  @Test
  public void testGeohashStrategy() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
