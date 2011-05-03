package org.apache.lucene.spatial.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.io.LineReader;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;

/**
 * Helper class to execute queries
 */
public class SpatialTestQuery {
  public String testname;
  public String line;
  public int lineNumber = -1;
  public SpatialArgs args;
  public List<String> ids = new ArrayList<String>();

  /**
   * Get Test Queries
   */
  public static Iterator<SpatialTestQuery> getTestQueries(
      final SpatialArgsParser parser,
      final SpatialContext shapeIO,
      final String name,
      final InputStream in ) throws IOException {
    return new LineReader<SpatialTestQuery>(new InputStreamReader(in,"UTF-8")) {

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
          test.args = parser.parse(line.substring(idx + 1).trim(), shapeIO);
          return test;
        }
        catch( Exception ex ) {
          throw new RuntimeException( "invalid query line: "+test.line, ex );
        }
      }
    };
  }
}
