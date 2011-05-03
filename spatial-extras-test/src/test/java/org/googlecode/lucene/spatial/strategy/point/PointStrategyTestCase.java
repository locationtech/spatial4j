package org.googlecode.lucene.spatial.strategy.point;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.point.PointFieldInfo;
import org.apache.lucene.spatial.strategy.point.PointStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


public class PointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {

  @Test
  public void testPointStrategyWithJts() throws IOException {
    TrieFieldHelper.FieldInfo tinfo = new TrieFieldHelper.FieldInfo();
    PointFieldInfo finfo = new PointFieldInfo( "point" );
    PointStrategy s = new PointStrategy( new SimpleSpatialContext(),
        tinfo, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );

    SpatialContext ctx = new JtsSpatialContext();
    executeQueries( s, ctx, finfo,
        SpatialMatchConcern.FILTER,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
