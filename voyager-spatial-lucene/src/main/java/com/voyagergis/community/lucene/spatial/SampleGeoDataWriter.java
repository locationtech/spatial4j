package com.voyagergis.community.lucene.spatial;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.io.sample.SampleDataWriter;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class SampleGeoDataWriter extends SampleDataWriter {

  protected final int maxLength;

  public SampleGeoDataWriter(File f,
      SpatialContext shapeIO,
      boolean bbox, int maxLength) throws IOException {
    super( f, shapeIO, bbox );
    this.maxLength = maxLength;
  }

  @Override
  protected String toString( String name, Shape shape ) {
    String v = shapeIO.toString( shape );
    if( maxLength > 0 && v.length() > maxLength ) {
      Geometry g = ((JtsSpatialContext)shapeIO).getGeometryFrom(shape);

      long last = v.length();
      Envelope env = g.getEnvelopeInternal();
      double mins = Math.min(env.getWidth(), env.getHeight());
      double div = 1000;
      while (v.length() > maxLength) {
        double tolerance = mins / div;
        System.out.println( name + " :: Simplifying long geometry: WKT.length=" + v.length() + " tolerance=" + tolerance);
        Geometry simple = TopologyPreservingSimplifier.simplify(g, tolerance);
        v = simple.toText();
        if (v.length() == last) {
          System.out.println( name + " :: Can not simplify geometry smaller then max. " + last);
          break;
        }
        last = v.length();
        div *= .70;
      }
    }
    return v;
  }
}
