package org.apache.lucene.spatial.strategy.geohash;

import java.io.IOException;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.test.SpatialMatchConcerns;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.apache.lucene.spatial.test.strategy.BaseGeohashStrategyTestCase;
import org.apache.lucene.spatial.test.strategy.BasePointStrategyTestCase;
import org.junit.Ignore;
import org.junit.Test;


public class GeohashStrategyTestCase extends BaseGeohashStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
