package org.apache.lucene.spatial.base;

import org.apache.lucene.spatial.base.exception.InvalidShapeException;

public interface ShapeIO
{
  /**
   * Read a shape from a given string (ie, X Y, XMin XMax... WKT)
   * 
   * (1) Point: X Y
   *   1.23 4.56
   *
   * (2) BOX: XMin YMin XMax YMax
   *   1.23 4.56 7.87 4.56
   *
   * (3) WKT
   *   POLYGON( ... )
   *   http://en.wikipedia.org/wiki/Well-known_text
   *
   */
  public Shape readShape( String value ) throws InvalidShapeException;
  

  /**
   * (2) BOX: XMin YMin XMax YMax
   *   1.23 4.56 7.87 4.56
   */
  public BBox readBBox( String value ) throws InvalidShapeException;

  public BBox readBBox( byte[] bytes, int offset, int length ) throws InvalidShapeException;
  
  public String toString( Shape shape );
  
  public byte[] toBytes( Shape shape );
}
