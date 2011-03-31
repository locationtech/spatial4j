package com.voyagergis.community.lucene.spatial.strategy.geohash;

import java.io.IOException;

import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.geohash.GeohashStrategy;
import org.apache.lucene.spatial.strategy.geohash.GridReferenceSystem;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import com.voyagergis.community.lucene.spatial.JtsSpatialContext;

public class GeohashStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Test
  public void testGeohashStrategy() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    JtsSpatialContext ctx = new JtsSpatialContext();
    int maxLength = GridReferenceSystem.getMaxPrecision();
    GridReferenceSystem grs = new GridReferenceSystem( ctx, maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    executeQueries( s, ctx, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
