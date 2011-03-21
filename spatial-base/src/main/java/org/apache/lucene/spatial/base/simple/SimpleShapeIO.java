package org.apache.lucene.spatial.base.simple;

import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.ShapeIO;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;

public class SimpleShapeIO implements ShapeIO
{
  public Shape readShape( String value ) throws InvalidShapeException
  {
    return readSimpleShape( value );
  }

  public static Shape readSimpleShape( String str )
  {
    if( str.length() < 1 ) {
      throw new InvalidShapeException( str );
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

  @Override
  public BBox readBBox(String value) throws InvalidShapeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BBox readBBox(byte[] bytes, int offset, int length)
      throws InvalidShapeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] toBytes(Shape shape) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString(Shape shape) {
    // TODO Auto-generated method stub
    return null;
  }
}
