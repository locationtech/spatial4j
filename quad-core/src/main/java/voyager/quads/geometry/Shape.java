package voyager.quads.geometry;

import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public abstract class Shape
{
  static final GeometryFactory geometryFactory = new GeometryFactory();

  public abstract double getWidth();
  public abstract double getHeight();
  public abstract IntersectCase intersection( Envelope env );


  /**
   * Given a string, make a shape... could be:
   *
   * point: X Y
   * envelope: XMin YMin XMax YMax
   * wkt: (http://en.wikipedia.org/wiki/Well-known_text)
   */
  public static final Shape parse( String str ) throws Exception {
    if( str.length() < 1 ) {
      throw new RuntimeException( "invalid string" );
    }
    if( !Character.isLetter(str.charAt(0)) ) {
      StringTokenizer st = new StringTokenizer( str, " " );
      double p0 = Double.parseDouble( st.nextToken() );
      double p1 = Double.parseDouble( st.nextToken() );
      if( st.hasMoreTokens() ) {
        double p2 = Double.parseDouble( st.nextToken() );
        double p3 = Double.parseDouble( st.nextToken() );
        return new EnvelopeShape( new Envelope( p0, p2, p1, p3 ) );
      }
      return new PointShape( new Coordinate(p0, p1) );
    }

    WKTReader reader = new WKTReader(geometryFactory);
    Geometry geo = reader.read( str );
    if( geo instanceof Point ) {
      return new PointShape(((Point)geo).getCoordinate());
    }
    return new GeometryShape( geo );
  }
}
