package org.apache.lucene.spatial.core;




public class Point2D implements Shape
{
  private double x;
  private double y;

  public Point2D(double x, double y)
  {
    this.x = x;
    this.y = y;
  }

  //----------------------------------------
  //----------------------------------------

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public Extent getExtent() {
    return new Rectangle( x, x, y, y );
  }

  @Override
  public IntersectCase intersect(Shape shape, Object context)
  {
    if( !(shape instanceof Extent) ) {
      throw new IllegalArgumentException( "Point can only be compared with another Extent" );
    }
    Extent ext = shape.getExtent();
    if( x >= ext.getMinX() &&
        x <= ext.getMaxX() &&
        y >= ext.getMinY() &&
        y <= ext.getMaxY() ){
      return IntersectCase.WITHIN;
    }
    return IntersectCase.OUTSIDE;
  }
}
