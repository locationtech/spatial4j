package com.spatial4j.core.shape.impl;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;

/**
 * Created by chris on 1/21/2014.
 */
public interface Ray {
  SpatialRelation relate(Rectangle r, Point prC, Point scratch);
  boolean contains(Point p);
  public double distanceUnbuffered(Point c);
  public int quadrant(Point c);
  public double getSlope();
  public double getIntercept();
  public double getBuf();
  public double getDistDenomInv();

}