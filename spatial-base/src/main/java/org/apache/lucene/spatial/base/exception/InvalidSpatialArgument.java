package org.apache.lucene.spatial.base.exception;

public class InvalidSpatialArgument extends RuntimeException 
{
  public InvalidSpatialArgument( String reason, Throwable cause )
  {
    super( reason, cause );
  }

  public InvalidSpatialArgument( String reason )
  {
    super( reason );
  }
}
