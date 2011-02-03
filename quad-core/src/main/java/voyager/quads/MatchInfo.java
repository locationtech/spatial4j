package voyager.quads;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchInfo
{
  private final boolean unique;

  public long timeToCalculate = -1;
  public int bboxLevel = -1;
  public int maxLevel = -1;
  public ArrayList<LevelMatchInfo> levels = new ArrayList<LevelMatchInfo>( 15 );
  public ArrayList<String> tokens = new ArrayList<String>();

  public MatchInfo() {
    unique = false;
  }

  public MatchInfo( boolean unique ) {
    this.unique = unique;
  }

  public LevelMatchInfo getLevelInfo( int level, boolean create )
  {
    if( level >= levels.size() ) {
      if( create ) {
        for( int i=levels.size(); i<level; i++ ) {
          levels.add( i, new LevelMatchInfo( i, unique ) );
        }
        LevelMatchInfo nnn = new LevelMatchInfo( level, unique );
        levels.add( level, nnn );
        return nnn;
      }
      return null;
    }

    LevelMatchInfo v = levels.get( level );
    if( v == null && create ) {
      v = new LevelMatchInfo( level, unique );
      levels.set( level, v );
    }
    return v;
  }

  /**
   * Make sure that each level has the right length
   * @throws Exception
   */
  public void validate() throws Exception
  {
    for( LevelMatchInfo level : levels ) {
      level.validate();
    }
  }

  public void printInfo()
  {
    System.out.println( "MatchInfo:" );
    System.out.println( " bboxLevel:"+bboxLevel );
    System.out.println( " maxLevel:"+maxLevel );
    if( timeToCalculate >= 0 ) {
      System.out.println( " time:"+timeToCalculate );
    }
    int total = 0;
    for( LevelMatchInfo level : levels ) {
      System.out.println( " Level: "+level.level );
      System.out.println( "  intersect: "+level.intersects );
      System.out.println( "  depth: "+level.depth );
      System.out.println( "  covers: "+level.covers );
      total += level.covers.size();
      total += level.intersects.size();
      total += level.depth.size();
    }
    System.out.println( " Total Tokens:"+total );
    System.out.println( " Description:"+(tokens.size())+" :: "+tokens );
//    for( String t : tokens ) {
//      System.out.println( " "+t );
//    }
  }

  public static Comparator<String> LEVEL_ORDER = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      int diff = o1.length() - o2.length();
      if( diff == 0 ) {
        return o1.compareTo(o2);
      }
      return diff;
    }
  };

  public static MatchInfo getMostlyWithinQueryTokens( List<String> description )
  {
    MatchInfo tokens = new MatchInfo();

    int firstLevel = -1;
    int level = -1;
    for( String t : description ) {
      int l = t.length()-2;
      if( level != l ) {
        level = l;
      }
      if( firstLevel < 0 ) {
        firstLevel = level;
      }

      LevelMatchInfo m = tokens.getLevelInfo( level, true );
      boolean covers = t.charAt( t.length()-1 ) == '*';
      StringBuffer buff = new StringBuffer();
      buff.append( t ).setLength( level+1 );
      if( covers ) {
        m.covers.add( buff.toString() );
        m.intersects.add( buff.toString() );

        // add covers one level deeper
        // intersect is guaranteed to match since a parent already does
        buff.append( 'X' );
        int down =level+1;
        LevelMatchInfo s = tokens.getLevelInfo( down, true );
        buff.setCharAt(down, 'A' ); s.covers.add( buff.toString() );
        buff.setCharAt(down, 'B' ); s.covers.add( buff.toString() );
        buff.setCharAt(down, 'C' ); s.covers.add( buff.toString() );
        buff.setCharAt(down, 'D' ); s.covers.add( buff.toString() );
        buff.setLength(down);
      }
      else { // intersect
        m.intersects.add( buff.toString() );
      }

      // go up 2 levels, but don't go past firstLevel
      for( int i=1; i<=2; i++ ) {
        l = level-i;
        if( firstLevel > l ) {
          break;
        }
        buff.setLength( l+1 );
        tokens.getLevelInfo( l, true )
          .intersects.add( buff.toString() );
      }
    }
    return tokens;
  }
}
