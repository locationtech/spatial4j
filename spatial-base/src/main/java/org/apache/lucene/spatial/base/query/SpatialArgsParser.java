package org.apache.lucene.spatial.base.query;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.exception.InvalidSpatialArgument;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;

public class SpatialArgsParser 
{
  public SpatialArgs parse(String v, ShapeIO reader) throws InvalidSpatialArgument, InvalidShapeException {
    int idx = v.indexOf('(');
    int edx = v.lastIndexOf(')');

    if (idx < 0 || idx > edx) {
      throw new InvalidSpatialArgument("missing parens: " + v, null);
    }

    SpatialOperation op = null;
    try {
      op = SpatialOperation.valueOf(v.substring(0, idx).trim());
    } catch(Exception ex) {
      throw new InvalidSpatialArgument("Unknown Operation: " + v.substring(0, idx), ex);
    }

    String body = v.substring(idx + 1, edx).trim();
    if (body.length() < 1) {
      throw new InvalidSpatialArgument("missing body : " + v, null);
    }

    Shape shape = reader.readShape(body);
    SpatialArgs args = new SpatialArgs(op,shape);
    
    if (v.length() > (edx + 1)) {
      body = v.substring( edx+1 ).trim();
      if (body.length() > 0) {
        Map<String,String> aa = parseMap(body);
        args.setCacheable( readBool(aa.remove("cache"), args.isCacheable() ));
        args.setCalculateScore( readBool(aa.remove("score"), args.isCalculateScore() ));
        args.setDistance( readDouble(aa.remove("distance"), null) );
        args.setRadius( readDouble(aa.remove("radius"), null) );
        if (!aa.isEmpty()) {
          throw new InvalidSpatialArgument("unused parameters: " + aa, null);
        }
      }
    }
    // Don't calculate a score if it is meaningless
    if (!op.isScoreIsMeaningful()) {
      args.setCalculateScore( false );
    }
    return args;
  }

  protected static Double readDouble(String v, Double defaultValue) {
    return v == null ? defaultValue : Double.valueOf(v);
  }

  protected static boolean readBool(String v, boolean defaultValue) {
    return v == null ? defaultValue : Boolean.parseBoolean(v);
  }

  protected static Map<String,String> parseMap(String body) {
    Map<String,String> map = new HashMap<String,String>();
    StringTokenizer st = new StringTokenizer(body, " \n\t");
    while (st.hasMoreTokens()) {
      String a = st.nextToken();
      int idx = a.indexOf('=');
      if (idx > 0) {
        String k = a.substring(0, idx);
        String v = a.substring(idx + 1);
        map.put(k, v);
      } else {
        map.put(a, a);
      }
    }
    return map;
  }
}
