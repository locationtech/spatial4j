package org.apache.lucene.spatial.quads;

import java.io.IOException;
import java.util.List;

public interface SpatialGrid
{
  /**
   * Get a list of tokens that describe the shape within the grid
   */
  public List<CharSequence> readCells( Shape geo );

  /**
   * Find a reasonable level of detail for a given shape
   */
  public int getBestLevel( Shape geo );

  /**
   * Get the shape for a given cell description
   */
  public Shape getCellShape( CharSequence seq );

  /**
   * Read a shape from a given string (ie, X Y, XMin XMax... WKT)
   */
  public Shape readShape( String str ) throws IOException;
}
