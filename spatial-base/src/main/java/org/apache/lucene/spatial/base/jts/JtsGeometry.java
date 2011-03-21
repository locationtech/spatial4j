package org.apache.lucene.spatial.base.jts;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Shape;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

public class JtsGeometry implements Shape
{
  public final Geometry geo;
  private final boolean _hasArea;
  
  public JtsGeometry(Geometry geo)
  {
    this.geo = geo;
    _hasArea = !((geo instanceof Lineal) || (geo instanceof Puntal));
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return _hasArea;
  }
  
  @Override
  public JtsEnvelope getBoundingBox() {
    return new JtsEnvelope( geo.getEnvelopeInternal() );
  }

  private GeometryFactory getGeometryFactory( Object context ) {
    if( context instanceof GeometryFactory ) {
      return (GeometryFactory)context;
    }
    return new GeometryFactory();
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    BBox ext = other.getBoundingBox();
    if( !ext.hasSize() ) {
      throw new IllegalArgumentException( "the query shape must cover some area (not a point or line)" );
    }

    // Quick test if this is outside
    Envelope gEnv = geo.getEnvelopeInternal();
    if (ext.getMinX() > gEnv.getMaxX() ||
        ext.getMaxX() < gEnv.getMinX() ||
        ext.getMinY() > gEnv.getMaxY() ||
        ext.getMaxY() < gEnv.getMinY() ){
      return IntersectCase.OUTSIDE;
    }

    Polygon qGeo = null;
    if( other instanceof JtsEnvelope ) {
      Envelope env = ((JtsEnvelope)other).envelope;
      qGeo = (Polygon) getGeometryFactory(context).toGeometry(env);
    }
    else if( other instanceof JtsGeometry ) {
      qGeo = (Polygon)((JtsGeometry)other).geo;
    }
    else if( other instanceof BBox ) {
      BBox e = (BBox)other;
      Envelope env = new Envelope( e.getMinX(), e.getMaxX(), e.getMinY(), e.getMaxY() );
      qGeo = (Polygon) getGeometryFactory(context).toGeometry(env);
    }
    else {
      throw new IllegalArgumentException( "this field only support intersectio with Extents or JTS Geometry" );
    }

    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects (qGeo, geo))
      return IntersectCase.OUTSIDE;

    //slower algorithm
    IntersectionMatrix matrix = geo.relate(qGeo);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers())
      return IntersectCase.CONTAINS;

    if (matrix.isCoveredBy())
      return IntersectCase.WITHIN;

    assert matrix.isIntersects();
    return IntersectCase.INTERSECTS;
  }
}
