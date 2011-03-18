package org.apache.lucene.spatial.core;

import java.util.Map;

import org.apache.lucene.spatial.core.exception.InvalidSpatialArgument;

public class WithinDistanceArgs extends SpatialArgs
{
  public Point2D center;
  public double radius;
  public String method;
  
  protected WithinDistanceArgs() {
    super( SpatialOperation.WithinDistance );
  }

  @Override
  public void read(String v, ShapeReader reader) throws InvalidSpatialArgument {

    Map<String,String> aa = parseMap( v );
    method = aa.remove( "method" );
    String distance = aa.remove( "distance" );
    String pts = aa.remove( "center" );
    if( !aa.isEmpty() ) {
      throw new InvalidSpatialArgument( "unused parameters: "+aa );
    }
    if( distance == null ) {
      throw new InvalidSpatialArgument( "missing distance" );
    }
    if( pts == null ) {
      throw new InvalidSpatialArgument( "missing center" );
    }
    String[] ccc = pts.split( "," );
    if( ccc.length != 2 ) {
      throw new InvalidSpatialArgument( "should have two points" );
    }
    center = new Point2D( Double.parseDouble(ccc[0]), Double.parseDouble(ccc[1]) );
  }
}
