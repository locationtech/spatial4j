package org.apache.lucene.spatial.grid.jts;


import org.apache.lucene.spatial.core.Extent;
import org.apache.lucene.spatial.core.IntersectCase;
import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.grid.SpatialGrid;

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
  public JtsEnvelope getExtent() {
    return new JtsEnvelope( p.getEnvelopeInternal() );
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    if( other instanceof Extent ) {
      Extent ext = other.getExtent();
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
