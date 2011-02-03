package voyager.quads.geometry;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

public class GeometryShape extends Shape
{
  public final Geometry shape;

  public GeometryShape( Geometry g ) {
    this.shape = g;
  }

  @Override
  public double getWidth() {
    return shape.getEnvelopeInternal().getWidth();
  }

  @Override
  public double getHeight() {
    return shape.getEnvelopeInternal().getHeight();
  }

  @Override
  public IntersectCase intersection( Envelope env )
  {
    Envelope gEnv = shape.getEnvelopeInternal();
    if(!gEnv.intersects(env)) {
      return IntersectCase.OUTSIDE;
    }

    Polygon rGeo = (Polygon) geometryFactory.toGeometry(env);//env must not be a point or line.
    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects (rGeo, shape))
      return IntersectCase.OUTSIDE;

    //slower algorithm
    IntersectionMatrix matrix = shape.relate(rGeo);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers())
      return IntersectCase.CONTAINS;

    if (matrix.isCoveredBy()) // not necessary for quad (but already calculated)
      return IntersectCase.WITHIN;

    assert matrix.isIntersects();
    return IntersectCase.INTERSECTS;
  }
}
