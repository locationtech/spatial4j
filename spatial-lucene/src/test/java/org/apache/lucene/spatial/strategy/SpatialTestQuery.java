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
  public String testname;
  public String line;
  public int lineNumber = -1;
  public SpatialArgs args;
  public List<String> ids = new ArrayList<String>();
  public boolean orderIsImportant = false;

  /**
   * Read queries from a file
   */
  public static Iterator<SpatialTestQuery> getTestQueries(
      final SpatialArgsParser parser,
      final ShapeIO reader,
      File file) throws IOException {
    return new LineReader<SpatialTestQuery>(file) {

      @Override
      public SpatialTestQuery parseLine(String line) {
        SpatialTestQuery test = new SpatialTestQuery();
        test.line = line;
        test.lineNumber = getLineNumber();

        try {
          // skip a comment
          if( line.startsWith( "[" ) ) {
            int idx = line.indexOf( ']' );
            if( idx > 0 ) {
              line = line.substring( idx+1 );
            }
          }

          int idx = line.indexOf('@');
          StringTokenizer st = new StringTokenizer(line.substring(0, idx));
          while (st.hasMoreTokens()) {
            test.ids.add(st.nextToken().trim());
          }
          test.args = parser.parse(line.substring(idx + 1).trim(), reader);
          return test;
        }
        catch( Exception ex ) {
          throw new RuntimeException( "invalid query line: "+test.line, ex );
        }
      }
    };
  }
}
