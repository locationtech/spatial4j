package org.apache.lucene.spatial.quads.linear;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.quads.IntersectCase;
import org.apache.lucene.spatial.quads.Point2D;
import org.apache.lucene.spatial.quads.Rectangle;
import org.apache.lucene.spatial.quads.Shape;
import org.apache.lucene.spatial.quads.ShapeExtent;
import org.apache.lucene.spatial.quads.SpatialGrid;



public class LinearSpatialGrid implements SpatialGrid
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

  public LinearSpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
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

  public LinearSpatialGrid()
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

  @Override
  public int getBestLevel(Shape geo) {
    ShapeExtent ext = geo.getExtent();
    double w = ext.getWidth();
    double h = ext.getHeight();

    for( int i=0; i<maxLevels; i++ ) {
      if( w > levelW[i] ) return i;
      if( h > levelH[i] ) return i;
    }
    return maxLevels;
  }

  public List<CharSequence> readCells( Shape geo )
  {
    ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
    int bboxLevel = getBestLevel( geo );
    int maxLevel = Math.min( maxLevels, bboxLevel+resolution );
    if( maxLevel < minResolution ) {
      maxLevel = minResolution;
    }

    build( xmid, ymid, 0, vals, new StringBuilder(), geo, maxLevel );
    return vals;
  }

  private void build( double x, double y, int level,
    List<CharSequence> matches, StringBuilder str,
    Shape geo, int maxLevel )
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
    List<CharSequence> matches, StringBuilder str,
    Shape geo, int maxLevel)
  {
    double w = levelW[level]/2;
    double h = levelH[level]/2;

    int strlen = str.length();
    ShapeExtent cell = makeExtent( cx-w, cx+w, cy-h, cy+h );
    IntersectCase v = geo.intersect( cell, this );
    if( IntersectCase.CONTAINS == v ) {
      str.append( c );
      str.append( '*' );
      matches.add( str.toString() );
    }
    else if( IntersectCase.OUTSIDE == v ) {
      // nothing
    }
    else { // IntersectCase.WITHIN, IntersectCase.INTERSECTS
      if( IntersectCase.WITHIN == v ) {
        str.append( Character.toLowerCase( c ) );
      }
      else {
        str.append( c );
      }

      int nextLevel = level+1;
      if( nextLevel >= maxLevel ) {
        str.append( '+' );
        matches.add( str.toString() );
      }
      else {
        build( cx,cy, nextLevel, matches, str, geo, maxLevel );
      }
    }
    str.setLength( strlen );
  }

  @Override
  public ShapeExtent getCell( CharSequence seq )
  {
    double xmin = this.xmin;
    double ymin = this.ymin;

    for( int i=0; i<seq.length() && i<maxLevels; i++ ) {
      char c = seq.charAt( i );
      if( 'A' == c || 'a' == c ) {
        ymin += levelH[i];
      }
      else if( 'B' == c || 'b' == c ) {
        xmin += levelW[i];
        ymin += levelH[i];
      }
      else if( 'C' == c || 'c' == c ) {
        // nothing really
      }
      else if( 'D' == c || 'd' == c ) {
        xmin += levelW[i];
      }
      else {
        throw new RuntimeException( "unexpected char: "+c );
      }
    }
    int len = seq.length()-1;
    return makeExtent(
        xmin, xmin+levelW[len],
        ymin, ymin+levelH[len] );
  }

  public static List<String> parseStrings(String cells)
  {
    ArrayList<String> tokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer( cells, "[], " );
    while( st.hasMoreTokens() ) {
      tokens.add( st.nextToken() );
    }
    return tokens;
  }

  // Subclasses could pick something explicit
  protected ShapeExtent makeExtent( double xmin, double xmax, double ymin, double ymax )
  {
    return new Rectangle( xmin, xmax, ymin, ymax );
  }

  @Override
  public Shape readShape(String str) throws IOException {
    if( str.length() < 1 ) {
      throw new RuntimeException( "invalid string" );
    }
    StringTokenizer st = new StringTokenizer( str, " " );
    double p0 = Double.parseDouble( st.nextToken() );
    double p1 = Double.parseDouble( st.nextToken() );
    if( st.hasMoreTokens() ) {
      double p2 = Double.parseDouble( st.nextToken() );
      double p3 = Double.parseDouble( st.nextToken() );
      return new Rectangle( p0, p2, p1, p3 );
    }
    return new Point2D(p0, p1 );
  }

  //------------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------------
}
