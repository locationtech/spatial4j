package org.apache.lucene.spatial.context;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.test.context.BaseSpatialContextTestCase;


/**
 */
public class SpatialContextTestCase extends BaseSpatialContextTestCase {

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
