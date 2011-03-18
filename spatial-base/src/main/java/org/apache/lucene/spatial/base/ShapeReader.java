package org.apache.lucene.spatial.base;

import java.io.IOException;

public interface ShapeReader
{
  /**
   * Read a shape from a given string (ie, X Y, XMin XMax... WKT)
   */
  public Shape readShape( String value ) throws IOException;
}
