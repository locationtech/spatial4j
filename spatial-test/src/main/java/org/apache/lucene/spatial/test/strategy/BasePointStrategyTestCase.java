package org.apache.lucene.spatial.test.strategy;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.point.PointFieldInfo;
import org.apache.lucene.spatial.strategy.point.PointStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public abstract class BasePointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {

  protected abstract SpatialContext getSpatialContext();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.shapeIO = getSpatialContext();
    this.strategy = new PointStrategy(shapeIO,
        new TrieFieldInfo(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER);
    this.fieldInfo = new PointFieldInfo( "point" );
  }

  @Test
  public void testCitiesWithinBBox() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
