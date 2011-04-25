
package org.googlecode.lucene.spatial.test.context;


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

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import com.googlecode.lucene.spatial.base.shape.JtsEnvelope;
import com.googlecode.lucene.spatial.base.shape.JtsGeometry;
import com.googlecode.lucene.spatial.base.shape.JtsPoint2D;


/**
 * Copied from SpatialContextTestCase
 */
public class JtsSpatialContextTestCase extends BaseSpatialContextTestCase {

  @Override
  protected JtsSpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }

  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    checkShapesImplementEquals( new Class[] {
      Point2D.class,
      PointDistanceShape.class,
      Rectangle.class,
      Shapes.class,
      JtsEnvelope.class,
      JtsPoint2D.class,
      JtsGeometry.class
    });
  }

  @Test
  public void testJtsShapeIO() throws Exception {
    final JtsSpatialContext io = getSpatialContext();
    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });

    checkBasicShapeIO( io, new WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) throws IOException {
        byte[] buff = io.toBytes( s );
        return io.readShape( buff, 0, buff.length );
      }
    });
  }
}
