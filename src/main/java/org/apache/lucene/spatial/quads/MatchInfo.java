package org.apache.lucene.spatial.quads;

import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.commons.lang.time.DurationFormatUtils;

public class MatchInfo 
{
  public long timeToCalculate = -1;
  public int bboxLevel = -1;
  public int maxLevel = -1;
  public ArrayList<LevelMatchInfo> levels = new ArrayList<LevelMatchInfo>( 15 );
  public ArrayList<String> tokens = new ArrayList<String>();

  public LevelMatchInfo getLevelInfo( int level, boolean create )
  {
    if( level >= levels.size() ) {
      if( create ) {
        LevelMatchInfo nnn = new LevelMatchInfo( level );
        levels.add( level, nnn );
        return nnn;
      }
      return null;
    }
    LevelMatchInfo v = levels.get( level );
    if( v == null && create ) {
      v = new LevelMatchInfo( level );
      levels.set( level, v );
    }
    return v;
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
  }
}
