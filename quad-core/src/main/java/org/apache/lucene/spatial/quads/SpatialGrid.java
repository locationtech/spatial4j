package org.apache.lucene.spatial.quads;

import java.util.List;

import com.vividsolutions.jts.io.ParseException;

public interface SpatialGrid
{
  public Shape readShape( String str ) throws ParseException;

  public int getBestLevel( Shape geo );

  public List<CharSequence> readCells( Shape geo );

  public Shape getCell( CharSequence seq );
}
