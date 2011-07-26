package org.googlecode.lucene.spatial.strategy.vector;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesFieldInfo;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class TwoDoublesStrategyTestCase extends StrategyTestCase<TwoDoublesFieldInfo> {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.ctx = new JtsSpatialContext();
    this.strategy = new TwoDoublesStrategy( new SimpleSpatialContext(),//TODO bug? why not ctx?
      new TrieFieldInfo(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );
    this.fieldInfo = new TwoDoublesFieldInfo( "vector2d" );
  }

  @Test
  public void testPointStrategyWithJts() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
