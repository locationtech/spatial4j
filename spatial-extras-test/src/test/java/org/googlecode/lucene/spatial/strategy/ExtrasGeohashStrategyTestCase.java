package org.googlecode.lucene.spatial.strategy;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.test.strategy.BaseGeohashStrategyTestCase;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;


public class ExtrasGeohashStrategyTestCase extends BaseGeohashStrategyTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }
}
