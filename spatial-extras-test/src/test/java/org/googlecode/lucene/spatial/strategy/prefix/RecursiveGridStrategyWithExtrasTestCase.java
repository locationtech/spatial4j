package org.googlecode.lucene.spatial.strategy.prefix;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;

import org.apache.lucene.spatial.base.prefix.geohash.GeohashPrefixTree;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class RecursiveGridStrategyWithExtrasTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    int maxLength = GeohashPrefixTree.getMaxLevelsPossible();
    this.ctx = new JtsSpatialContext();
    GeohashPrefixTree grid = new GeohashPrefixTree(
        ctx, maxLength );
    this.strategy = new RecursivePrefixTreeStrategy( grid );
    //((RecursiveGridStrategy)strategy).setDistErrPct(0.1);//little faster
    this.fieldInfo = new SimpleSpatialFieldInfo( "geohash" );
    this.storeShape = false;//unnecessary
  }

  /**
   * For now, the only difference from the Simple version is that this uses JtsSpatialContext
   */
  @Test
  public void testWorldCitiesWithinBox() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }

  @Test
  public void testPolygonIndex() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_STATES_POLY);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_States_Intersects_BBox);
  }
}
