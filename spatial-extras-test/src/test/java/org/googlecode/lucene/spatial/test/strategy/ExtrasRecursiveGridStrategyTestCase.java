package org.googlecode.lucene.spatial.test.strategy;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.test.strategy.BaseRecursivePrefixTreeStrategyTestCase;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


public class ExtrasRecursiveGridStrategyTestCase extends BaseRecursivePrefixTreeStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }
}
