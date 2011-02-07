package org.apache.lucene.spatial.quads;



public class Rectangle implements ShapeExtent
{
  private double minX;
  private double maxX;
  private double minY;
  private double maxY;

  public Rectangle(double minX, double maxX, double minY, double maxY)
  {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public double getHeight() {
    return maxY - minY;
  }


  @Override
  public double getWidth() {
    return maxX - minX;
  }

  @Override
  public double getMaxX() {
    return maxX;
  }

  @Override
  public double getMaxY() {
    return maxY;
  }

  @Override
  public double getMinX() {
    return minX;
  }

  @Override
  public double getMinY() {
    return minY;
  }

  @Override
  public boolean hasSize() {
    return maxX > minX && maxY > minY;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public ShapeExtent getExtent() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape shape, SpatialGrid grid)
  {
    if( !(shape instanceof ShapeExtent) ) {
      throw new IllegalArgumentException( "Rectangle can only be compared with another Extent" );
    }
    ShapeExtent ext = shape.getExtent();
    if (ext.getMinX() > maxX ||
        ext.getMaxX() < minX ||
        ext.getMinY() > maxY ||
        ext.getMaxY() < minY ){
      return IntersectCase.OUTSIDE;
    }

    if( ext.getMinX() >= minX &&
        ext.getMaxX() <= maxX &&
        ext.getMinY() >= minY &&
        ext.getMaxY() <= maxY ){
      return IntersectCase.CONTAINS;
    }

    if( minX >= ext.getMinY() &&
        maxX <= ext.getMaxX() &&
        minY >= ext.getMinY() &&
        maxY <= ext.getMaxY() ){
      return IntersectCase.WITHIN;
    }
    return IntersectCase.INTERSECTS;
  }
  
  public String toString()
  {
    return "["+minX+","+maxX+","+minY+","+maxY+"]";
  }
}
