package org.apache.lucene.spatial.base.io.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContextProvider;
import org.apache.lucene.spatial.base.shape.Shape;

public class SampleDataWriter {

  protected final PrintWriter out;
  protected final SpatialContext shapeIO;
  protected final boolean bbox;

  public SampleDataWriter(File f, SpatialContext shapeIO, boolean bbox) throws IOException {
    this.shapeIO=shapeIO;
    this.bbox = bbox;

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
    this( f, SpatialContextProvider.getContext(), false );
  }

  public SampleDataWriter(File f, boolean bbox ) throws IOException {
    this( f, SpatialContextProvider.getContext(), bbox );
  }

  public SampleDataWriter(File f, int maxLength ) throws IOException {
    this( f, SpatialContextProvider.getContext(), false );
  }


  protected String toString( String name, Shape shape ) {
    return shapeIO.toString( shape );
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
