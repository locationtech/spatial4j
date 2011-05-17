package org.googlecode.lucene.spatial.strategy.point;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.point.PointFieldInfo;
import org.apache.lucene.spatial.strategy.point.PointStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class PointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.shapeIO = new JtsSpatialContext();
    this.strategy = new PointStrategy( new SimpleSpatialContext(),//TODO bug? why not shapeIO?
      new TrieFieldInfo(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );
    this.fieldInfo = new PointFieldInfo( "point" );
  }

  @Test
  public void testPointStrategyWithJts() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
