package voyager.quads.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class PointShape extends Shape
{
  public final Coordinate p;

  public PointShape( Coordinate p ) {
    this.p = p;
  }

  @Override
  public double getWidth() {
    return 0;
  }

  @Override
  public double getHeight() {
    return 0;
  }

  @Override
  public IntersectCase intersection( Envelope env )
  {
    if( env.contains( p ) ) {
      return IntersectCase.WITHIN;
    }
    return IntersectCase.OUTSIDE;
  }
}
