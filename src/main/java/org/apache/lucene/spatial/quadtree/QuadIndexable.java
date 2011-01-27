package org.apache.lucene.spatial.quadtree;

public interface QuadIndexable
{
  public double getWidth();
  public double getHeight();
  public MatchState test( double xmin, double xmax, double ymin, double ymax );
}
