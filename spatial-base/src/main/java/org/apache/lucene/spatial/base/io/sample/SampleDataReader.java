package org.apache.lucene.spatial.base.io.sample;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.spatial.base.io.LineReader;
import org.apache.lucene.spatial.base.shape.ShapeIO;

public class SampleDataReader extends LineReader<SampleData> {

  public SampleDataReader(Reader r) throws IOException {
    super( r );
  }

  public SampleDataReader(File f) throws IOException {
    super( f );
  }

  @Override
  public SampleData parseLine(String line) {
    return new SampleData( line );
  }
}
