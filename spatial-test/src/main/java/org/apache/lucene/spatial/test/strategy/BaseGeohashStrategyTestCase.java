package org.apache.lucene.spatial.test.strategy;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.DynamicPrefixStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public abstract class BaseGeohashStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  protected abstract SpatialContext getSpatialContext();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    int maxLength = GeohashSpatialPrefixGrid.getMaxLevelsPossible();
    // SimpleIO
    this.shapeIO = getSpatialContext();
    this.strategy = new DynamicPrefixStrategy(new GeohashSpatialPrefixGrid(
        shapeIO, maxLength ));
    this.fieldInfo = new SimpleSpatialFieldInfo( "geohash" );
  }

  @Test
  public void testGeohashStrategy() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
