package org.apache.lucene.spatial.base.jts;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Shape;

import com.vividsolutions.jts.geom.Envelope;

public class JtsEnvelope implements BBox
{
  public final Envelope envelope;

  public JtsEnvelope(Envelope envelope)
  {
    this.envelope = envelope;
  }

  public JtsEnvelope(double x1, double x2, double y1, double y2)
  {
    this.envelope = new Envelope( x1,x2,y1,y2 );
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return getWidth() > 0 && getHeight() > 0;
  }
  
  public double getArea()
  {
    return getWidth() * getHeight();
  }

  public boolean getCrossesDateLine()
  {
    return false;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public double getHeight() {
    return envelope.getWidth();
  }

  @Override
  public double getWidth() {
    return envelope.getHeight();
  }

  @Override
  public double getMaxX() {
    return envelope.getMaxX();
  }

  @Override
  public double getMaxY() {
    return envelope.getMaxY();
  }

  @Override
  public double getMinX() {
    return envelope.getMinX();
  }

  @Override
  public double getMinY() {
    return envelope.getMinY();
  }

  @Override
  public boolean hasSize() {
    return !envelope.isNull();
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public BBox getBoundingBox() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    if( other instanceof BBox ) {
      BBox ext = other.getBoundingBox();
      if (ext.getMinX() > envelope.getMaxX() ||
          ext.getMaxX() < envelope.getMinX() ||
          ext.getMinY() > envelope.getMaxY() ||
          ext.getMaxY() < envelope.getMinY() ){
        return IntersectCase.OUTSIDE;
      }

      if( ext.getMinX() >= envelope.getMinX() &&
          ext.getMaxX() <= envelope.getMaxX() &&
          ext.getMinY() >= envelope.getMinY() &&
          ext.getMaxY() <= envelope.getMaxY() ){
        return IntersectCase.CONTAINS;
      }

      if( envelope.getMinX() >= ext.getMinY() &&
          envelope.getMaxX() <= ext.getMaxX() &&
          envelope.getMinY() >= ext.getMinY() &&
          envelope.getMaxY() <= ext.getMaxY() ){
        return IntersectCase.WITHIN;
      }
      return IntersectCase.INTERSECTS;
    }
    else if( other instanceof JtsGeometry ) {

    }
    throw new IllegalArgumentException( "JtsEnvelope can be compared with Envelope or Geogmetry" );
  }

  @Override
  public String toString()
  {
    return envelope.toString();
  }
}
