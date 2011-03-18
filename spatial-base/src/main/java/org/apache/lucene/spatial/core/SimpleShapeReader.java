package org.apache.lucene.spatial.core;

import java.io.IOException;
import java.util.StringTokenizer;

public class SimpleShapeReader implements ShapeReader
{
  public Shape readShape( String value ) throws IOException
  {
    return readSimpleShape( value );
  }

  public static Shape readSimpleShape( String str )
  {
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
}
