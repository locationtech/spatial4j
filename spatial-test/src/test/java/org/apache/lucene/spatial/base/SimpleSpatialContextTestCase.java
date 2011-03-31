package org.apache.lucene.spatial.base;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.shape.PointDistanceShape;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.Shapes;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.apache.lucene.spatial.test.helper.ShapeTestHelper;
import org.junit.Test;


/**
 */
public class SimpleSpatialContextTestCase {

  @Test
  public void testArgsParser() throws Exception {
    ShapeTestHelper.checkArgParser(new SimpleSpatialContext());
  }

  @Test
  public void testImplementsEqualsAndHash() throws Exception {
    ShapeTestHelper.checkShapesImplementEquals( new Class[] {
      Point2D.class,
      PointDistanceShape.class,
      Rectangle.class,
      Shapes.class
    });
  }

  @Test
  public void testSimpleShapeIO() throws Exception {
    final SimpleSpatialContext io = new SimpleSpatialContext();
    ShapeTestHelper.checkBasicShapeIO( io, new ShapeTestHelper.WriteReader() {
      @Override
      public Shape writeThenRead(Shape s) {
        String buff = io.toString( s );
        return io.readShape( buff );
      }
    });
  }

  @Test
  public void testSimpleIntersection() throws Exception {
    final SimpleSpatialContext io = new SimpleSpatialContext();
    ShapeTestHelper.checkBBoxIntersection(io);
  }
}
