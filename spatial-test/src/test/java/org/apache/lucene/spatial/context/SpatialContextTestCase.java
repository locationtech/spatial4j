package org.apache.lucene.spatial.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.AbstractSpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.PointDistanceShape;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.Shapes;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.apache.lucene.spatial.test.context.BaseSpatialContextTestCase;
import org.junit.Assert;
import org.junit.Test;


/**
 */
public class SpatialContextTestCase extends BaseSpatialContextTestCase {

  @Override
  protected AbstractSpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
