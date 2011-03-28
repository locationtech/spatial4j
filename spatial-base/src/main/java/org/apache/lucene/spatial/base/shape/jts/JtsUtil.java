package org.apache.lucene.spatial.base.shape.jts;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * @author Chris Male
 */
public class JtsUtil {

  private JtsUtil() {}

  public static Geometry getGeometryFrom(Shape shape, GeometryFactory factory) {
    if (JtsGeometry.class.isInstance(shape)) {
      return ((JtsGeometry)shape).geo;
    }
    if (JtsPoint2D.class.isInstance(shape)) {
      return ((JtsPoint2D)shape).getPoint();
    }
    if (JtsEnvelope.class.isInstance(shape)) {
      return factory.toGeometry(((JtsEnvelope)shape).envelope);
    }

    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }
}
