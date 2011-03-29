package org.apache.lucene.spatial.base.io.geonames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

public class GeonamesReader implements Iterator<Geoname> {

  private int count = 0;
  private BufferedReader reader;
  private String nextLine;
  private boolean closeWhenDone = false;

  public GeonamesReader(Reader r) throws IOException {
    if (BufferedReader.class.isInstance(r)) {
      reader = (BufferedReader) r;
    } else {
      reader = new BufferedReader(r);
    }
    nextLine = reader.readLine();
  }

  public GeonamesReader(File f) throws IOException {
    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
    nextLine = reader.readLine();
    closeWhenDone = true;
  }

  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public Geoname next() {
    Geoname val = null;
    if (nextLine != null) {
      val = new Geoname(nextLine);
      count++;
    }

    if (reader != null) {
      try {
        nextLine = reader.readLine();
        if (nextLine == null && closeWhenDone) {
          reader.close();
          reader = null;
        }
      } catch (IOException ioe) {
        throw new RuntimeException("IOException thrown while reading/closing reader", ioe);
      }
    }
    return val;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int getCount() {
    return count;
  }
}
