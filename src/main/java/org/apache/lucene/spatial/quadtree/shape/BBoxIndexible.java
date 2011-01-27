package org.apache.lucene.spatial.quadtree.shape;

import org.apache.lucene.spatial.quadtree.QuadIndexable;

import org.apache.lucene.spatial.quadtree.MatchState;

public class BBoxIndexible implements QuadIndexable
{
  protected double _xmax;
  protected double _xmin;
  protected double _ymax;
  protected double _ymin;

  public BBoxIndexible( double xmin, double xmax, double ymin, double ymax )
  {
    _xmin = xmin;
    _xmax = xmax;
    _ymin = ymin;
    _ymax = ymax;
  }

  public double getMinX()   { return _xmin; }
  public double getMinY()   { return _ymin; }
  public double getWidth()  { return _xmax - _xmin; }
  public double getHeight() { return _ymax - _ymin; }

  @Override
  public String toString()
  {
    return "("+_xmin+", "+_xmax+")("+_ymin+","+_ymax+")";
  }

  @Override
  public MatchState test(double xmin, double xmax, double ymin, double ymax)
  {
    // check for miss
    if( _xmax <= xmin ) return MatchState.MISS;
    if( _xmin >= xmax ) return MatchState.MISS;
    if( _ymax <= ymin ) return MatchState.MISS;
    if( _ymin >= ymax ) return MatchState.MISS;

    // check covers
    if( _xmin <= xmin && _xmax >= xmax &&
        _ymin <= ymin && _ymax >= ymax ){
      return MatchState.COVERS;
    }

    return MatchState.TOUCHES;
  }
}
