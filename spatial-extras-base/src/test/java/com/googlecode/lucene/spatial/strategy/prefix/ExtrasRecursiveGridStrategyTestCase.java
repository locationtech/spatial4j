package com.googlecode.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.strategy.prefix.BaseRecursivePrefixTreeStrategyTestCase;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


public class ExtrasRecursiveGridStrategyTestCase extends BaseRecursivePrefixTreeStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }
}
