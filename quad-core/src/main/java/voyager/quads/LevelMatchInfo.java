package voyager.quads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class LevelMatchInfo
{
  public final int level;
  public final Collection<String> covers;
  public final Collection<String> depth;
  public final Collection<String> intersects;

  /**
   * @param level
   * @param unique should the terms be forced unique?  not required when using recursio
   */
  public LevelMatchInfo( int level, boolean unique ) {
    this.level = level;

    if( unique ) {
      covers = new HashSet<String>();
      depth = new HashSet<String>();
      intersects = new HashSet<String>();
    }
    else {
      covers = new ArrayList<String>();
      depth = new ArrayList<String>();
      intersects = new ArrayList<String>();
    }
  }

  /**
   * All the strings at a given level should have the same length
   * @throws Exception
   */
  public void validate() throws Exception
  {
    validateStringLength( covers );
    validateStringLength( depth );
    validateStringLength( intersects );
  }

  private void validateStringLength( Collection<String> list ) throws Exception {
    for( String s : list ) {
      if( s.length() != (level+1) ) {
        throw new Exception( "wrong length: "+level + " '"+s+"'" );
      }
    }
  }
}
