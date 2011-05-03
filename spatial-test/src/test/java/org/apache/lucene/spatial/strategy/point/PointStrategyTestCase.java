package org.apache.lucene.spatial.strategy.point;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.test.strategy.BasePointStrategyTestCase;


public class PointStrategyTestCase extends BasePointStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
