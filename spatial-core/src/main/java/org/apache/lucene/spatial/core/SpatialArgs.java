package org.apache.lucene.spatial.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.core.exception.InvalidSpatialArgument;

public abstract class SpatialArgs 
{
  public final SpatialOperation op;
  public boolean cacheable = true;
  public boolean calculateScore = true;
  
  protected SpatialArgs( SpatialOperation op ) {
    this.op = op;
  }
  
  public abstract void read(  String v, ShapeReader reader ) throws IOException;
  
  /**
   * Check if the arguments make sense -- throw an exception if not
   */ 
  public void validate() throws InvalidSpatialArgument
  {
    // OK by default
  }
  

  public static SpatialArgs parse( String v, ShapeReader reader ) throws InvalidSpatialArgument
  {
    int idx = v.indexOf( '[' );
    int edx = v.indexOf( ']' );
    if( idx < 0 || idx > edx ) {
      throw new InvalidSpatialArgument( "missing parens: "+v, null );
    }

    SpatialOperation op = null;
    try {
      op = SpatialOperation.valueOf( v.substring( 0, idx ).trim() );
    }
    catch( Exception ex ) {
      throw new InvalidSpatialArgument( "Unknown Operation: "+v.substring(0, idx), ex );
    }
    SpatialArgs args = null;
    if( op == SpatialOperation.WithinDistance ) {
      args = new WithinDistanceArgs();
    }
    else {
      args = new GeometryArgs( op );
    }
    String body = v.substring( idx+1, edx ).trim();
    if( body.length() < 1 ) {
      throw new InvalidSpatialArgument( "missing body : "+v, null );
    }
    try {
      args.read(body, reader);
    }
    catch (IOException e) {
      throw new InvalidSpatialArgument( "Unable read: "+body, e );
    }
    if( v.length() > (edx+1) ) {
      body = v.substring( edx+1 ).trim();
      if( body.length() > 0 ) {
        Map<String,String> aa = parseMap( body );
        args.cacheable = readBool( aa.remove("cache"), args.cacheable );
        args.calculateScore = readBool( aa.remove("score"), args.calculateScore );
        if( !aa.isEmpty() ) {
          throw new InvalidSpatialArgument( "unused parameters: "+aa, null );
        }
      }
    }
    // Don't calculate a score if it is meaningless
    if( !op.scoreIsMeaningful ) {
      args.calculateScore = false;
    }
    return args;
  }
  
  protected static boolean readBool( String v, boolean defaultValue )
  {
    if( v == null ) {
      return defaultValue;
    }
    return Boolean.parseBoolean( v );
  }
  
  protected static Map<String,String> parseMap( String body )
  {
    Map<String,String> map = new HashMap<String,String>();
    StringTokenizer st = new StringTokenizer( body, " \n\t" );
    while( st.hasMoreTokens() ) {
      String a = st.nextToken();
      int idx = a.indexOf( '=' );
      if( idx > 0 ) {
        String k = a.substring(0,idx);
        String v = a.substring(idx+1);
        map.put( k, v );
      }
      else {
        map.put( a, a );
      }
    }
    return map;
  }
}
