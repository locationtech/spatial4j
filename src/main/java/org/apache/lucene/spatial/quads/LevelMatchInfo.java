package org.apache.lucene.spatial.quads;

import java.util.ArrayList;
import java.util.List;

public class LevelMatchInfo
{
  public final int level;
  public final List<String> covers = new ArrayList<String>();
  public final List<String> depth = new ArrayList<String>();
  public final List<String> intersects = new ArrayList<String>();

  public LevelMatchInfo( int level ) {
    this.level = level;
  }
}
