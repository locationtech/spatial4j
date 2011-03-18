package org.apache.lucene.spatial.core;

import java.io.IOException;

import org.apache.lucene.spatial.core.exception.InvalidSpatialArgument;

public class GeometryArgs extends SpatialArgs
{
  public Shape shape;
  
  protected GeometryArgs( SpatialOperation op ) {
    super( op );
  }

  @Override
  public void read(String v, ShapeReader reader) throws IOException {
    shape = reader.readShape( v );
  }
  
  @Override
  public void validate() throws InvalidSpatialArgument
  {
    if( op.targetNeedsArea && !shape.hasArea() ) {
      throw new InvalidSpatialArgument( op.name() + " only supports geometry with area" );
    }
  }
}
