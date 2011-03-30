package org.apache.lucene.spatial.base.io.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.ShapeIOProvider;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class SampleDataWriter {

  protected final PrintWriter out;
  protected final ShapeIO shapeIO;
  protected final boolean bbox;
  protected final int maxLength;

  public SampleDataWriter(File f, ShapeIO shapeIO, boolean bbox, int maxLength) throws IOException {
    this.shapeIO=shapeIO;
    this.bbox = bbox;
    this.maxLength= maxLength;

    out = new PrintWriter( new OutputStreamWriter(
        new FileOutputStream(f), "UTF8") );

    out.print( "#id" );
    out.print( '\t' );
    out.print( "name" );
    out.print( '\t' );
    out.print( "shape" );
    out.print( '\t' );
    out.println();
    out.flush();
  }

  public SampleDataWriter(File f ) throws IOException {
    this( f, ShapeIOProvider.getShapeIO(), false, -1 );
  }

  public SampleDataWriter(File f, boolean bbox ) throws IOException {
    this( f, ShapeIOProvider.getShapeIO(), bbox, -1 );
  }

  public SampleDataWriter(File f, int maxLength ) throws IOException {
    this( f, new JtsShapeIO(DistanceUnits.KILOMETERS), false, maxLength );
  }


  protected String toString( String name, Shape shape ) {
    String v = shapeIO.toString( shape );
    if( maxLength > 0 && v.length() > maxLength ) {
      Geometry g = ((JtsShapeIO)shapeIO).getGeometryFrom(shape);

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

  public void write(String id, String name, double x, double y)  throws IOException {
    this.write(id, name, shapeIO.makePoint(x, y) );
  }

  public void write(String id, String name, Shape shape)  throws IOException {

    String geo = toString( name, bbox?shape.getBoundingBox():shape );
    out.print( id );
    out.print( '\t' );
    out.print( name );
    out.print( '\t' );
    out.print( geo );
    out.print( '\t' );
    out.println();
    out.flush();
  }

  public void close() {
    out.close();
  }
}
