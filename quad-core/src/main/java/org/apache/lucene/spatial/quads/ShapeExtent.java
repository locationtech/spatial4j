package org.apache.lucene.spatial.quads;


public interface ShapeExtent extends Shape
{
  public double getWidth();
  public double getHeight();

  public double getMinX();
  public double getMinY();
  public double getMaxX();
  public double getMaxY();

  public boolean hasSize();
}
