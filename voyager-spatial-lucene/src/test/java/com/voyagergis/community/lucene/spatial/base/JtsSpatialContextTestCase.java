package com.voyagergis.community.lucene.spatial.base;

import java.io.IOException;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.test.helper.ShapeTestHelper;
import org.junit.Test;

import com.voyagergis.community.lucene.spatial.JtsSpatialContext;
import com.voyagergis.community.lucene.spatial.shape.JtsEnvelope;
import com.voyagergis.community.lucene.spatial.shape.JtsGeometry;
import com.voyagergis.community.lucene.spatial.shape.JtsPoint2D;


/**
 */
public class JtsSpatialContextTestCase {

  @Test
  public void testArgsParser() throws Exception {
    ShapeTestHelper.checkArgParser(new SimpleSpatialContext());
  }

  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    ShapeTestHelper.checkShapesImplementEquals( new Class[] {
      JtsEnvelope.class,
      JtsPoint2D.class,
      JtsGeometry.class
    });
  }

  @Test
  public void testSimpleIntersection() throws Exception {
    final SimpleSpatialContext io = new SimpleSpatialContext();
    ShapeTestHelper.checkBBoxIntersection(io);
  }

  @Test
  public void testSimpleShapeIO() throws Exception {
    final JtsSpatialContext io = new JtsSpatialContext();
    ShapeTestHelper.checkBasicShapeIO( io, new ShapeTestHelper.WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });

    ShapeTestHelper.checkBasicShapeIO( io, new ShapeTestHelper.WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) throws IOException {
        byte[] buff = io.toBytes( s );
        return io.readShape( buff, 0, buff.length );
      }
    });
  }
}
