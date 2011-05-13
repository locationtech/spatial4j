package org.apache.lucene.spatial.test.benchmark;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.SpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;


public interface StrategyAware<T extends SpatialFieldInfo> {

  T createFieldInfo();

  SpatialStrategy<T> createSpatialStrategy();

  SpatialContext getSpatialContext();
}
