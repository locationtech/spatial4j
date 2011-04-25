package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.test.strategy.BaseGeohashStrategyTestCase;


public class GeohashStrategyTestCase extends BaseGeohashStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
