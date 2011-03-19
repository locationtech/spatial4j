package org.apache.lucene.spatial.base.jts;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.ShapeIO;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class WKTShapeReader implements ShapeIO
{ 
  public GeometryFactory factory;
  
  public WKTShapeReader()
  {
    factory = new GeometryFactory();
  }
  
  public WKTShapeReader( GeometryFactory f )
  {
    factory = f;
  }
  
  public Shape readShape( String str ) throws InvalidShapeException
  {
    if( str.length() < 1 ) {
      throw new InvalidShapeException( str );
    }
    if( !Character.isLetter(str.charAt(0)) ) {
      StringTokenizer st = new StringTokenizer( str, " " );
      double p0 = Double.parseDouble( st.nextToken() );
      double p1 = Double.parseDouble( st.nextToken() );
      if( st.hasMoreTokens() ) {
        double p2 = Double.parseDouble( st.nextToken() );
        double p3 = Double.parseDouble( st.nextToken() );
        return new JtsEnvelope( new Envelope( p0, p2, p1, p3 ) );
      }
      return new JtsPoint2D( factory.createPoint(new Coordinate(p0, p1)) );
    }

    WKTReader reader = new WKTReader(factory);
    try {
      Geometry geo = reader.read( str );
      if( geo instanceof Point ) {
        return new JtsPoint2D((Point)geo);
      }
      return new JtsGeometry( geo );
    }
    catch( com.vividsolutions.jts.io.ParseException ex ) {
      throw new InvalidShapeException( "error reading WKT", ex );
    }
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
