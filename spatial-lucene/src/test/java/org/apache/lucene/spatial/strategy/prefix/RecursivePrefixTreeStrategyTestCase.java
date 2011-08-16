package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.junit.Before;


public class RecursivePrefixTreeStrategyTestCase extends BaseRecursivePrefixTreeStrategyTestCase {

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
