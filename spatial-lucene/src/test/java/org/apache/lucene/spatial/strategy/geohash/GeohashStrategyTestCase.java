package org.apache.lucene.spatial.strategy.geohash;

import java.io.IOException;

import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.junit.Test;

public class GeohashStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Test
  public void testGeohashStrategy() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    int maxLength = GridReferenceSystem.getMaxPrecision();
    GridReferenceSystem grs = new GridReferenceSystem(
        new SimpleShapeIO(), maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    // SimpleIO
    executeQueries( s, grs.shapeIO, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );


    if( false ) {
      grs = new GridReferenceSystem(
          new JtsShapeIO(), maxLength );
      s = new GeohashStrategy( grs );

    // With JTS
    executeQueries( s, grs.shapeIO, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
    }
  }
}
