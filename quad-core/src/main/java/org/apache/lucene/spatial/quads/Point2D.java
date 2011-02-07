package org.apache.lucene.spatial.quads;



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
  public ShapeExtent getExtent() {
    return new Rectangle( x, x, y, y );
  }

  @Override
  public IntersectCase intersect(Shape shape, SpatialGrid grid)
  {
    if( !(shape instanceof ShapeExtent) ) {
      throw new IllegalArgumentException( "Point can only be compared with another Extent" );
    }
    ShapeExtent ext = shape.getExtent();
    if( x >= ext.getMinX() &&
        x <= ext.getMaxX() &&
        y >= ext.getMinY() &&
        y <= ext.getMaxY() ){
      return IntersectCase.WITHIN;
    }
    return IntersectCase.OUTSIDE;
  }
}
