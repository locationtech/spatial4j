package org.apache.lucene.spatial.test.strategy;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.point.PointFieldInfo;
import org.apache.lucene.spatial.strategy.point.PointStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.apache.lucene.spatial.test.SpatialMatchConcerns;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;


public abstract class BasePointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {

  protected abstract SpatialContext getSpatialContext();

  protected PointStrategy getStrategy() {
    TrieFieldHelper.FieldInfo tinfo = new TrieFieldHelper.FieldInfo();
    return new PointStrategy( getSpatialContext(),
        tinfo, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );
  }

  @Test
  public void testCitiesWithinBBox() throws IOException {
    PointFieldInfo finfo = new PointFieldInfo( "point" );
    PointStrategy s = getStrategy();

    executeQueries( s, getSpatialContext(), finfo,
        SpatialMatchConcerns.FILTER,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
