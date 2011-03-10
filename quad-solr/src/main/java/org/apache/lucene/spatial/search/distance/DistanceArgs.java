package org.apache.lucene.spatial.search.distance;

import org.apache.lucene.spatial.geometry.shape.Point2D;
import org.apache.lucene.spatial.search.SpatialArgs;

public class DistanceArgs extends SpatialArgs
{
  public Point2D point = null;
  
  // Used for limiting a range
  public Double min = null;
  public Double max = null;
}