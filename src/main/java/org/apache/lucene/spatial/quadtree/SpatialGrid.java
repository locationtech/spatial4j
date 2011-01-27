package org.apache.lucene.spatial.quadtree;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.lucene.spatial.quadtree.shape.BBoxIndexible;


/**
 * This is a linear example... but hopefully could support:
 *
 *  http://en.wikipedia.org/wiki/Mercator_projection
 *  http://en.wikipedia.org/wiki/Sinusoidal_projection
 *
 *
 *
 * Consider something like:
 *  http://code.google.com/p/h2database/source/browse/trunk/h2/src/main/org/h2/tools/MultiDimension.java
 *  http://code.google.com/p/h2database/source/browse/trunk/h2/src/test/org/h2/test/db/TestMultiDimension.java
 *
 */
public class SpatialGrid
{
  final double xmin;
  final double xmax;
  final double ymin;
  final double ymax;
  final double xmid;
  final double ymid;
  final int maxLevels;

  final double gridW;
  final double gridH;

  final double[] levelW;
  final double[] levelH;
  final int[]    levelS; // side
  final int[]    levelN; // number

  int resolution = 4; // how far down past the 'bbox level'


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

  public int getBBoxLevel( QuadIndexable geo )
  {
    double w = geo.getWidth();
    double h = geo.getHeight();

    for( int i=0; i<maxLevels; i++ ) {
      if( w > levelW[i] ) return i;
      if( h > levelH[i] ) return i;
    }
    return maxLevels;
  }

  public List<String> read( QuadIndexable geo, MutableInt bboxLevel )
  {
    int maxLevel = getBBoxLevel( geo );
    if( bboxLevel != null ) {
      bboxLevel.setValue( maxLevel );
    }
    maxLevel = Math.min( maxLevels, maxLevel+resolution );
    ArrayList<String> vals = new ArrayList<String>();

    build( xmid, ymid, 0, vals, new StringBuilder(), geo, maxLevel );
    return vals;
  }

  private void build( double x, double y, int level,
    List<String> matches, StringBuilder str,
    QuadIndexable geo, int maxLevel )
  {
    double w = levelW[level]/2;
    double h = levelH[level]/2;

    // Z-Order
    // http://en.wikipedia.org/wiki/Z-order_%28curve%29
    checkBattenberg( 'A', x-w, y+h, level, matches, str, geo, maxLevel );
    checkBattenberg( 'B', x+w, y+h, level, matches, str, geo, maxLevel );
    checkBattenberg( 'C', x-w, y-h, level, matches, str, geo, maxLevel );
    checkBattenberg( 'D', x+w, y-h, level, matches, str, geo, maxLevel );

    // possibly consider hilbert curve
    // http://en.wikipedia.org/wiki/Hilbert_curve
    // http://blog.notdot.net/2009/11/Damn-Cool-Algorithms-Spatial-indexing-with-Quadtrees-and-Hilbert-Curves
    // if we actually use the range property in the query, this could be useful

  }

  private void checkBattenberg(
    char c, double cx, double cy,
    int level,
    List<String> matches, StringBuilder str,
    QuadIndexable geo, int maxLevel)
  {
    double w = levelW[level]/2;
    double h = levelH[level]/2;

    int strlen = str.length();
    MatchState v = geo.test( cx-w, cx+w, cy-h, cy+h );
    if( MatchState.COVERS == v ) {
      str.append( c );
      str.append( '*' );
      matches.add( str.toString() );
    }
    else if( MatchState.TOUCHES == v ) {
      str.append( c );
      int nextLevel = level+1;
      if( nextLevel >= maxLevel ) {
        str.append( '-' );
        matches.add( str.toString() );
      }
      else {
        build( cx,cy, nextLevel, matches, str, geo, maxLevel );
      }
    }
    str.setLength( strlen );
  }

  public BBoxIndexible getRectangle( CharSequence seq )
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
      else if( '*' == c || '-' == c ) {
        return new BBoxIndexible(
            xmin, xmin+2*levelW[i],
            ymin, ymin+2*levelH[i] );
      }
      else {
        throw new RuntimeException( "unexpected char: "+c );
      }
    }

    // this happens when the string goes to the full resolution
    return new BBoxIndexible(
        xmin, xmin+levelW[maxLevels-1],
        ymin, ymin+levelH[maxLevels-1] );
  }
}
