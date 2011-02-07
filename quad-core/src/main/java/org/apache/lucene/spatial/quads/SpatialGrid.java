package org.apache.lucene.spatial.quads;

import java.io.IOException;
import java.util.List;

public interface SpatialGrid
{
  public Shape readShape( String str ) throws IOException;

  public int getBestLevel( Shape geo );

  public List<CharSequence> readCells( Shape geo );

  public Shape getCell( CharSequence seq );
}
