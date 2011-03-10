package org.apache.lucene.spatial.core;

import org.apache.lucene.spatial.grid.SpatialGrid;


/**
 * When minX > maxX, this will assume it is world coordinates that cross the
 * date line using degrees
 */
public class Rectangle implements Extent
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

  public double getArea()
  {
    // CrossedDateline = true;
    if (minX > maxX) {
      return Math.abs(maxX + 360.0 - minX) * Math.abs(maxY - minY);
    }
    return Math.abs(maxX - minX) * Math.abs(maxY - minY);
  }
  
  public boolean getCrossesDateLine()
  {
    return (minX > maxX);
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
  public Extent getExtent() {
    return this;
  }

  @Override
  public IntersectCase intersect(Shape shape, Object context)
  {
    if( !(shape instanceof Extent) ) {
      throw new IllegalArgumentException( "Rectangle can only be compared with another Extent" );
    }
    Extent ext = shape.getExtent();
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

  @Override
  public String toString()
  {
    return "["+minX+","+maxX+","+minY+","+maxY+"]";
  }
}
