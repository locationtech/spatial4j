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
    
    executeQueries("us-states.txt", "test-us-IsWithin-BBox.txt", new JtsShapeIO(), s, new BBoxFieldInfo( "bbox" ));
  }
}
