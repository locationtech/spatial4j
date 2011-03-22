/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.base;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.exception.InvalidSpatialArgument;

public class SpatialArgs
{
  public final SpatialOperation op;
  public Shape shape;
  public boolean cacheable = true;
  public boolean calculateScore = true;

  protected SpatialArgs( SpatialOperation op ) {
    this.op = op;
  }

  /**
   * Check if the arguments make sense -- throw an exception if not
   */
  public void validate() throws InvalidSpatialArgument
  {
    if( op.targetNeedsArea && !shape.hasArea() ) {
      throw new InvalidSpatialArgument( op.name() + " only supports geometry with area" );
    }
  }


  public static SpatialArgs parse( String v, ShapeIO reader ) throws InvalidSpatialArgument, InvalidShapeException
  {
    int idx = v.indexOf( '(' );
    int edx = v.lastIndexOf( ')' );
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
    SpatialArgs args = new SpatialArgs( op );
    String body = v.substring( idx+1, edx ).trim();
    if( body.length() < 1 ) {
      throw new InvalidSpatialArgument( "missing body : "+v, null );
    }
    args.shape = reader.readShape( body );

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
