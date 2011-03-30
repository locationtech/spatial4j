package org.apache.lucene.spatial.base.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

public abstract class LineReader<T> implements Iterator<T> {

  private int count = 0;
  private BufferedReader reader;
  private String nextLine;
  private boolean closeWhenDone = false;

  public abstract T parseLine( String line );

  protected void readComment( String line ) {

  }

  public LineReader(Reader r) throws IOException {
    if (r instanceof BufferedReader) {
      reader = (BufferedReader) r;
    } else {
      reader = new BufferedReader(r);
    }
    next();
  }

  public LineReader(File f) throws IOException {
    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
    closeWhenDone = true;
    next();
  }

  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public T next() {
    T val = null;
    if (nextLine != null) {
      val = parseLine(nextLine);
      count++;
    }

    if (reader != null) {
      try {
        while( true ) {
          nextLine = reader.readLine();
          if (nextLine == null ) {
            if( closeWhenDone ) {
              reader.close();
              reader = null;
            }
          }
          else if( nextLine.startsWith( "#" ) ) {
            readComment( nextLine );
            continue;
          }
          break;
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
