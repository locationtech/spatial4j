package org.apache.lucene.spatial.base.jts;

import java.text.NumberFormat;
import java.util.Locale;
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

public class JTSShapeIO implements ShapeIO
{
  public GeometryFactory factory;

  public JTSShapeIO()
  {
    factory = new GeometryFactory();
  }

  public JTSShapeIO( GeometryFactory f )
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

    if( str.startsWith( "RADIUS(" ) ) {
      try {
        int idx = str.indexOf( '(' );
        int edx = str.lastIndexOf( ')' );
        StringTokenizer st = new StringTokenizer( str.substring(idx+1,edx), " " );
        double p0 = Double.parseDouble( st.nextToken() );
        double p1 = Double.parseDouble( st.nextToken() );
        double rr = Double.parseDouble( st.nextToken() );
        Point p = factory.createPoint( new Coordinate( p0, p1 ) );
        return new JtsRadius2D( new JtsPoint2D(p), rr );
      }
      catch( Exception ex ) {
        throw new InvalidShapeException( "invalid radius: "+str, ex );
      }
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
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public BBox readBBox(byte[] bytes, int offset, int length) throws InvalidShapeException
  {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public byte[] toBytes(Shape shape) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public String writeBBox(BBox bbox)
  {
    NumberFormat nf = NumberFormat.getInstance( Locale.US );
    nf.setGroupingUsed( false );
    nf.setMaximumFractionDigits( 6 );
    nf.setMinimumFractionDigits( 6 );
    return
      nf.format( bbox.getMinX() ) + " " +
      nf.format( bbox.getMinY() ) + " " +
      nf.format( bbox.getMaxX() ) + " " +
      nf.format( bbox.getMaxY() );
  }

  @Override
  public String toString(Shape shape)
  {
    if( shape instanceof org.apache.lucene.spatial.base.Point ) {
      NumberFormat nf = NumberFormat.getInstance( Locale.US );
      nf.setGroupingUsed( false );
      nf.setMaximumFractionDigits( 6 );
      nf.setMinimumFractionDigits( 6 );
      org.apache.lucene.spatial.base.Point point = (org.apache.lucene.spatial.base.Point)shape;
      return nf.format( point.getX() ) + " " + nf.format( point.getY() );
    }
    else if( shape instanceof BBox ) {
      writeBBox( (BBox)shape );
    }
    else if( shape instanceof JtsGeometry ) {
      JtsGeometry geo = (JtsGeometry)shape;
      return geo.geo.toText();
    }
    return shape.toString();
  }
}
