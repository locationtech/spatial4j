package voyager.quads;

import java.text.NumberFormat;
import java.util.Collections;

import voyager.quads.geometry.IntersectCase;
import voyager.quads.geometry.Shape;

import com.vividsolutions.jts.geom.Envelope;



/**
 * This is a linear example... but hopefully could support:
 *  http://en.wikipedia.org/wiki/Mercator_projection
 *  http://en.wikipedia.org/wiki/Sinusoidal_projection
 *
 * Consider something like:
 *  http://code.google.com/p/h2database/source/browse/trunk/h2/src/main/org/h2/tools/MultiDimension.java
 *  http://code.google.com/p/h2database/source/browse/trunk/h2/src/test/org/h2/test/db/TestMultiDimension.java
 *
 */
public class SpatialGrid
{
  public final double xmin;
  public final double xmax;
  public final double ymin;
  public final double ymax;
  public final double xmid;
  public final double ymid;
  public final int maxLevels;

  public final double gridW;
  public final double gridH;

  final double[] levelW;
  final double[] levelH;
  final int[]    levelS; // side
  final int[]    levelN; // number

  public int minResolution = 6; // Go at least this deep
  public int resolution = 4; // how far down past the 'bbox level'

  public SpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
  {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
    this.maxLevels = maxLevels;

    levelW = new double[maxLevels];
    levelH = new double[maxLevels];
    levelS = new int[maxLevels];
    levelN = new int[maxLevels];

    gridW = xmax - xmin;
    gridH = ymax - ymin;
    this.xmid = xmin + gridW/2.0;
    this.ymid = ymin + gridH/2.0;
    levelW[0] = gridW/2.0;
    levelH[0] = gridH/2.0;
    levelS[0] = 2;
    levelN[0] = 4;

    for( int i=1; i<maxLevels; i++ ) {
      levelW[i] = levelW[i-1]/2.0;
      levelH[i] = levelH[i-1]/2.0;
      levelS[i] = levelS[i-1]*2;
      levelN[i] = levelN[i-1]*4;
    }
  }

  public SpatialGrid()
  {
    this( -180, 180, -90, 90, 12 );
  }

  public void printInfo()
  {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits( 5 );
    nf.setMinimumFractionDigits( 5 );
    nf.setMinimumIntegerDigits( 3 );

    for( int i=0; i<maxLevels; i++ ) {
      System.out.println( i + "]\t"+nf.format(levelW[i])+"\t"+nf.format(levelH[i])+"\t"+levelS[i]+"\t"+(levelS[i]*levelS[i]) );
    }
  }

  public int getBBoxLevel( Shape geo )
  {
    double w = geo.getWidth();
    double h = geo.getHeight();

    for( int i=0; i<maxLevels; i++ ) {
      if( w > levelW[i] ) return i;
      if( h > levelH[i] ) return i;
    }
    return maxLevels;
  }

  public MatchInfo read( Shape geo )
  {
    long startTime = System.currentTimeMillis();
    MatchInfo vals = new MatchInfo();
    vals.bboxLevel = getBBoxLevel( geo );
    vals.maxLevel = Math.min( maxLevels, vals.bboxLevel+resolution );
    if( vals.maxLevel < minResolution ) {
      vals.maxLevel = minResolution;
    }

    build( xmid, ymid, 0, vals, new StringBuilder(), geo );
    Collections.sort( vals.tokens, MatchInfo.LEVEL_ORDER );
    vals.timeToCalculate = System.currentTimeMillis()-startTime;
    return vals;
  }

  private void build( double x, double y, int level,
    MatchInfo matches, StringBuilder str,
    Shape geo )
  {
    double w = levelW[level]/2;
    double h = levelH[level]/2;

    // Z-Order
    // http://en.wikipedia.org/wiki/Z-order_%28curve%29
    checkBattenberg( 'A', x-w, y+h, level, matches, str, geo );
    checkBattenberg( 'B', x+w, y+h, level, matches, str, geo );
    checkBattenberg( 'C', x-w, y-h, level, matches, str, geo );
    checkBattenberg( 'D', x+w, y-h, level, matches, str, geo );

    // possibly consider hilbert curve
    // http://en.wikipedia.org/wiki/Hilbert_curve
    // http://blog.notdot.net/2009/11/Damn-Cool-Algorithms-Spatial-indexing-with-Quadtrees-and-Hilbert-Curves
    // if we actually use the range property in the query, this could be useful
  }

  private void checkBattenberg(
    char c, double cx, double cy,
    int level,
    MatchInfo matches, StringBuilder str,
    Shape geo)
  {
    double w = levelW[level]/2;
    double h = levelH[level]/2;

    LevelMatchInfo info = matches.getLevelInfo( level, true );

    int strlen = str.length();
    Envelope cell = new Envelope( cx-w, cx+w, cy-h, cy+h );
    IntersectCase v = geo.intersection( cell );
    if( IntersectCase.CONTAINS == v ) {
      str.append( c );
      info.covers.add( str.toString() );

      str.append( '*' );
      matches.tokens.add( str.toString() );
    }
    else if( IntersectCase.OUTSIDE == v ) {
      // nothing
    }
    else { // IntersectCase.WITHIN, IntersectCase.INTERSECTS
      str.append( c );

      int nextLevel = level+1;
      if( nextLevel >= matches.maxLevel ) {
        info.depth.add( str.toString() );
        str.append( '-' ); // not necessary?
        matches.tokens.add( str.toString() );
      }
      else {
        info.intersects.add( str.toString() );
        build( cx,cy, nextLevel, matches, str, geo );
      }
    }
    str.setLength( strlen );
  }

  public Envelope getRectangle( CharSequence seq )
  {
    double xmin = this.xmin;
    double ymin = this.ymin;

    for( int i=0; i<seq.length() && i<maxLevels; i++ ) {
      char c = seq.charAt( i );
      if( 'A' == c ) {
        ymin += levelH[i];
      }
      else if( 'B' == c ) {
        xmin += levelW[i];
        ymin += levelH[i];
      }
      else if( 'C' == c ) {
        // nothing really
      }
      else if( 'D' == c ) {
        xmin += levelW[i];
      }
      else {
        throw new RuntimeException( "unexpected char: "+c );
      }
    }
    int len = seq.length()-1;
    return new Envelope(
        xmin, xmin+levelW[len],
        ymin, ymin+levelH[len] );
  }

  //------------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------------
}
