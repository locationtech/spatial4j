package org.apache.lucene.spatial.strategy.bbox;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.jts.JtsSpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.junit.Test;

public class BBoxStrategyTestCase extends StrategyTestCase<BBoxFieldInfo> {

  public void executeQueries( SpatialContext io, String data, String ... tests ) throws IOException {
    BBoxStrategy s = new BBoxStrategy();
    s.trieInfo = new TrieFieldHelper.FieldInfo();
    s.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    BBoxFieldInfo finfo = new BBoxFieldInfo( "bbox" );
    executeQueries( s, io, finfo, data, tests );
  }

  @Test
  public void testBBoxPolyWithSimple() throws IOException {
    executeQueries( new SimpleSpatialContext(),
        DATA_STATES_BBOX,
        QTEST_States_IsWithin_BBox,
        QTEST_States_Intersects_BBox );
  }

  @Test
  public void testBBoxPointsSimple() throws IOException {
    executeQueries( new SimpleSpatialContext(),
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }

  @Test
  public void testBBoxPolyWithJts() throws IOException {
    executeQueries( new JtsSpatialContext(),
        DATA_STATES_POLY,
        QTEST_States_IsWithin_BBox,
        QTEST_States_Intersects_BBox );
  }

  @Test
  public void testBBoxPointsJts() throws IOException {
    executeQueries( new JtsSpatialContext(),
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
