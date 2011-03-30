package org.apache.lucene.spatial.strategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.io.LineReader;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.ShapeIO;

/**
 * Helper class to execute queries
 */
public class SpatialTestQuery {
  public int testId = -1;
  public SpatialArgs args;
  public List<String> ids = new ArrayList<String>();
  public boolean orderIsImportant = false;

  /**
   * Read queries from a file
   */
  public static Iterator<SpatialTestQuery> getTestQueries(
      final SpatialArgsParser parser,
      final ShapeIO reader, File file ) throws IOException {
    return new LineReader<SpatialTestQuery>( file ) {

      @Override
      public SpatialTestQuery parseLine(String line) {
        SpatialTestQuery test = new SpatialTestQuery();
        test.testId = getLineNumber();
        int idx = line.indexOf( '@' );
        StringTokenizer st = new StringTokenizer( line.substring(0,idx) );
        while( st.hasMoreTokens() ) {
          test.ids.add( st.nextToken().trim() );
        }
        test.args = parser.parse(line.substring(idx+1).trim(), reader);
        return test;
      }
    };
  }
}
