package org.apache.lucene.spatial.base.io.geonames;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.spatial.base.io.LineReader;

public class GeonamesReader extends LineReader<Geoname> {

  public GeonamesReader(Reader r) throws IOException {
    super( r );
  }

  public GeonamesReader(File f) throws IOException {
    super( f );
  }

  @Override
  public Geoname parseLine(String line) {
    return new Geoname( line );
  }
}
