package com.voyagergis.community.lucene.spatial.strategy;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContextProvider;
import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.bbox.BBoxFieldInfo;
import org.apache.lucene.spatial.strategy.bbox.BBoxStrategy;
import org.apache.lucene.spatial.strategy.prefix.PrefixGridStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import com.voyagergis.community.lucene.spatial.JtsSpatialContext;

public class PrefixGridStrategyWithJtsTestCase extends StrategyTestCase<SimpleSpatialFieldInfo>{

  public void executeQueries( SpatialContext io, String data, String ... tests ) throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo("geo");
    PrefixGridStrategy s 
      = new PrefixGridStrategy(
          new LinearPrefixGrid(-180, 180, -90, 90, 12, io), 0);

    executeQueries( s, io, finfo, data, tests );
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
