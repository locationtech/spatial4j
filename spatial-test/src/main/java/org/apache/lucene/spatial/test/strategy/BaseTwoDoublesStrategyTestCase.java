package org.apache.lucene.spatial.test.strategy;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesFieldInfo;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public abstract class BaseTwoDoublesStrategyTestCase extends StrategyTestCase<TwoDoublesFieldInfo> {

  protected abstract SpatialContext getSpatialContext();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.ctx = getSpatialContext();
    this.strategy = new TwoDoublesStrategy(ctx,
        new TrieFieldInfo(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER);
    this.fieldInfo = new TwoDoublesFieldInfo( "vector2d" );
  }

  @Test
  public void testCitiesWithinBBox() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
