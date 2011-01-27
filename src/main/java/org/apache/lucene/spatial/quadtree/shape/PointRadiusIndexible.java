package org.apache.lucene.spatial.quadtree.shape;

import org.apache.lucene.spatial.quadtree.MatchState;

public class PointRadiusIndexible extends BBoxIndexible
{
  private double _centerX;
  private double _centerY;
  private double _radius;

  public PointRadiusIndexible( double cX, double cY, double radius )
  {
    super( cX - radius, cX+radius, cY-radius, cY+radius );
    _centerX = cX;
    _centerY = cY;
    _radius = radius;
  }

  @Override
  public String toString()
  {
    return "("+_centerX+", "+_centerY+")("+_radius+")";
  }

  @Override
  public MatchState test(double xmin, double xmax, double ymin, double ymax)
  {
    if( xmin == 4 && ymin == 2) {
      System.out.println( "XXX" );
    }

    // check for miss
    if( _xmax <= xmin ) return MatchState.MISS;
    if( _xmin >= xmax ) return MatchState.MISS;
    if( _ymax <= ymin ) return MatchState.MISS;
    if( _ymin >= ymax ) return MatchState.MISS;

    double w = xmax-xmin;
    double h = ymax-ymin;
    double w2 = w/2;
    double h2 = h/2;

    double dX = _centerX - xmin - w2;
    double dY = _centerY - ymin - h2;

    double distX = Math.abs(dX);
    double distY = Math.abs(dY);

    if (distX > w2 && distY > h2 ) {
      if (distX > (w2 + _radius)) { return MatchState.MISS; }
      if (distY > (h2 + _radius)) { return MatchState.MISS; }

      double d2 = (distX - w2)*(distX - w2) +
                  (distY - h2)*(distY - h2);

      if( d2 > (_radius*_radius) ) {
        return MatchState.MISS;
      }
      else if( d2 <= (_radius*2) ){
        return MatchState.COVERS;
      }
    }
    else {
      // Check if BBOX covers
      if( _xmin < xmin && _xmax > xmax &&
          _ymin < ymin && _ymax > ymax ){
        return MatchState.COVERS;
      }
    }

    // TODO... should be able to get more things that actually cover
    // stuff that is outside of the rotated rectangle

    return MatchState.TOUCHES;
  }
}
