package org.apache.lucene.spatial.core;


public interface Extent extends Shape
{
  public double getWidth();
  public double getHeight();

  public double getMinX();
  public double getMinY();
  public double getMaxX();
  public double getMaxY();

  public double getArea(); // optional
  public boolean getCrossesDateLine();

  /**
   * Width and height have a meaningful value
   */
  public boolean hasSize();
}
