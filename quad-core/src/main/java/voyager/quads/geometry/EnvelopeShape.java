package voyager.quads.geometry;

import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeShape extends Shape
{
  public final Envelope shape;

  public EnvelopeShape( Envelope env ) {
    this.shape = env;
    if( env.isNull() ) {
      throw new IllegalArgumentException( "envelope can not be null" );
    }
  }

  @Override
  public double getWidth() {
    return shape.getWidth();
  }

  @Override
  public double getHeight() {
    return shape.getHeight();
  }

  @Override
  public IntersectCase intersection( Envelope env )
  {
    if( shape.intersects( env ) ) {
      if( shape.covers( env ) ) {
        return IntersectCase.CONTAINS;
      }
//      if( env.covers( shape ) ) {  // not necessary for quad
//        return IntersectCase.WITHIN;
//      }
      return IntersectCase.INTERSECTS;
    }
    return IntersectCase.OUTSIDE;
  }
}
