
package com.googlecode.lucene.spatial.context;


import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import com.googlecode.lucene.spatial.base.shape.JtsEnvelope;
import com.googlecode.lucene.spatial.base.shape.JtsGeometry;
import com.googlecode.lucene.spatial.base.shape.JtsPoint;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.Shapes;
import org.apache.lucene.spatial.base.shape.simple.CircleImpl;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.spatial.base.shape.simple.RectangleImpl;
import org.apache.lucene.spatial.context.BaseSpatialContextTestCase;
import org.junit.Test;

import java.io.IOException;


/**
 * Copied from SpatialContextTestCase
 */
public class JtsSpatialContextTestCase extends BaseSpatialContextTestCase {

  @Override
  protected JtsSpatialContext getSpatialContext() {
    return new JtsSpatialContext();
  }

  @Override
  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    checkShapesImplementEquals( new Class[] {
      PointImpl.class,
      CircleImpl.class,
      RectangleImpl.class,
      Shapes.class,
      JtsEnvelope.class,
      JtsPoint.class,
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
