package org.apache.lucene.spatial.core.exception;

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
