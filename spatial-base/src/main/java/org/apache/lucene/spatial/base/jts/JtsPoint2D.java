package org.apache.lucene.spatial.base.jts;


import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Shape;

import com.vividsolutions.jts.geom.Point;


public class JtsPoint2D implements Shape
{
  private Point p;

  public JtsPoint2D( Point p )
  {
    this.p = p;
  }

  //----------------------------------------
  //----------------------------------------

  public Point getPoint() {
    return p;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return false;
  }
  
  @Override
  public JtsEnvelope getBoundingBox() {
    return new JtsEnvelope( p.getEnvelopeInternal() );
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    if( other instanceof BBox ) {
      BBox ext = other.getBoundingBox();
      if( p.getX() >= ext.getMinX() &&
          p.getX() <= ext.getMaxX() &&
          p.getY() >= ext.getMinY() &&
          p.getY() <= ext.getMaxY() ){
        return IntersectCase.WITHIN;
      }
      return IntersectCase.OUTSIDE;
    }
    else if( other instanceof JtsGeometry ) {
      if( ((JtsGeometry)other).geo.contains( p ) ) {
        return IntersectCase.WITHIN;
      }
      return IntersectCase.OUTSIDE;
    }
    throw new IllegalArgumentException( "JtsEnvelope can be compared with Envelope or Geogmetry" );
  }
}
