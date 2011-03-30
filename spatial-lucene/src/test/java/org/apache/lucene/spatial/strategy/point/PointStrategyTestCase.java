package org.apache.lucene.spatial.strategy.point;

import java.io.IOException;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.junit.Test;

public class PointStrategyTestCase extends StrategyTestCase<PointFieldInfo> {

  @Test
  public void testBBoxStrategy() throws IOException {
    TrieFieldHelper.FieldInfo tinfo = new TrieFieldHelper.FieldInfo();
    PointFieldInfo finfo = new PointFieldInfo( "point" );

    PointStrategy s = new PointStrategy( new SimpleShapeIO(),
        tinfo, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );

    if( false ) {
    // With JTS
    executeQueries( s, new JtsShapeIO(), finfo,
        DATA_STATES_POLY,
        QTEST_States_IsWithin_BBox,
        QTEST_States_Intersects_BBox );

    // With SimpleIO
    executeQueries( s, new SimpleShapeIO(), finfo,
        DATA_STATES_BBOX,
        QTEST_States_IsWithin_BBox,
        QTEST_States_Intersects_BBox );

    }

    executeQueries( s, new SimpleShapeIO(), finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
