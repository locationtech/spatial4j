package org.apache.lucene.spatial.strategy.point;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.jts.JtsSpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.junit.Test;


public class PointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {

  @Test
  public void testPointStrategyWithSimple() throws IOException {
    TrieFieldHelper.FieldInfo tinfo = new TrieFieldHelper.FieldInfo();
    PointFieldInfo finfo = new PointFieldInfo( "point" );
    PointStrategy s = new PointStrategy( new SimpleSpatialContext(),
        tinfo, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );

    executeQueries( s, new SimpleSpatialContext(), finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
  

  @Test
  public void testPointStrategyWithJts() throws IOException {
    TrieFieldHelper.FieldInfo tinfo = new TrieFieldHelper.FieldInfo();
    PointFieldInfo finfo = new PointFieldInfo( "point" );
    PointStrategy s = new PointStrategy( new SimpleSpatialContext(),
        tinfo, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );

    executeQueries( s, new JtsSpatialContext(), finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
