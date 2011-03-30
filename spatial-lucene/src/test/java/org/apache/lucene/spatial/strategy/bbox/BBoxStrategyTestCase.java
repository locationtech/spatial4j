package org.apache.lucene.spatial.strategy.bbox;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.junit.Test;

import java.io.IOException;

public class BBoxStrategyTestCase extends StrategyTestCase<BBoxFieldInfo> {

  @Test
  public void testSpatialSearch() throws IOException {
    BBoxStrategy s = new BBoxStrategy();
    s.trieInfo = new TrieFieldHelper.FieldInfo();
    s.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    
    executeQueries( s, new JtsShapeIO(), new BBoxFieldInfo( "bbox" ),
        DATA_US_STATES,
        QTEST_US_IsWithin_BBox,
        QTEST_US_Intersects_BBox );
  }
}
