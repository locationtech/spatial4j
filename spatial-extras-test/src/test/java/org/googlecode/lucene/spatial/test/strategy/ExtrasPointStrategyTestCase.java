package org.googlecode.lucene.spatial.test.strategy;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.test.strategy.BaseTwoDoublesStrategyTestCase;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


public class ExtrasPointStrategyTestCase extends BaseTwoDoublesStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }
}
