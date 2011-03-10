package voyager.quads.utils.geonames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

public class GeonamesReader implements Iterator<Geoname>
{
  int count = 0;
  BufferedReader reader;
  String nextLine;
  boolean closeWhenDone = false;

  public GeonamesReader( Reader r ) throws IOException {
    if( r instanceof BufferedReader ) {
      reader = (BufferedReader)r;
    }
    else {
      reader = new BufferedReader( r );
    }
    nextLine = reader.readLine();
  }

  public GeonamesReader( File f ) throws IOException
  {
    reader = new BufferedReader( new InputStreamReader(new FileInputStream(f), "UTF-8") );
    nextLine = reader.readLine();
    closeWhenDone = true;
  }


  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public Geoname next()
  {
    Geoname val = null;
    if( nextLine != null ) {
      try {
        val = Geoname.parse( nextLine );
        count++;
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    if( reader != null ) {
      try {
        nextLine = reader.readLine();
        if( nextLine == null && closeWhenDone ) {
          reader.close();
          reader = null;
        }
      }
      catch (IOException e) {
        e.printStackTrace();
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
