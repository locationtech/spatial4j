package org.apache.lucene.spatial.quadtree.shape;

import org.apache.lucene.spatial.quadtree.QuadIndexable;

import org.apache.lucene.spatial.quadtree.MatchState;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class JTSIndexible implements QuadIndexable
{
  // ???? not sure what to do with this...
  static final GeometryFactory factory = new GeometryFactory();

  final Geometry geo;


  public JTSIndexible( String wkt ) throws ParseException
  {
    WKTReader reader = new WKTReader( factory );
    geo = reader.read( wkt );
  }

  public JTSIndexible( Geometry geo )
  {
    this.geo = geo;
  }

  @Override
  public double getHeight() {
    return geo.getEnvelopeInternal().getWidth();
  }

  @Override
  public double getWidth() {
    return geo.getEnvelopeInternal().getHeight();
  }

  @Override
  public String toString()
  {
    return "("+geo+")";
  }

  @Override
  public MatchState test(double xmin, double xmax, double ymin, double ymax)
  {
    // do quick culling first
    Envelope env = geo.getEnvelopeInternal();
    if( env.getMaxX() <= xmin ) return MatchState.MISS;
    if( env.getMinX() >= xmax ) return MatchState.MISS;
    if( env.getMaxY() <= ymin ) return MatchState.MISS;
    if( env.getMinY() >= ymax ) return MatchState.MISS;

    // Must be some better way... this seems kinda slow...
    CoordinateSequence coords = PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(
        new double[] {
          xmin,ymin,
          xmin,ymax,
          xmax,ymax,
          xmax,ymin,
          xmin,ymin
        }, 2 );

    LinearRing ring = factory.createLinearRing( coords );
    Geometry box = new Polygon( ring, null, factory );

    if( geo.covers( box ) ) {
      return MatchState.COVERS;
    }
    if( geo.intersects( box ) ) {
      return MatchState.TOUCHES;
    }
    return MatchState.MISS;
  }
}
