package org.apache.lucene.spatial.strategy.vector;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;

public class TwoDoublesStrategyTestCase extends BaseTwoDoublesStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
