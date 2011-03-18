package org.apache.lucene.spatial.core.grid;

import java.util.List;

import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.core.ShapeReader;

public interface SpatialGrid extends ShapeReader
{
  public static final char COVER = '*';
  public static final char INTERSECTS = '+';
  
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
}
