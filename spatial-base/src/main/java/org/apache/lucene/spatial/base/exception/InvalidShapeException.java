package org.apache.lucene.spatial.base.exception;

public class InvalidShapeException extends RuntimeException
{
  public InvalidShapeException( String reason, Throwable cause )
  {
    super( reason, cause );
  }

  public InvalidShapeException( String reason )
  {
    super( reason );
  }
}
