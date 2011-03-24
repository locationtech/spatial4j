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

package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class JTSGeoQueryBuilder implements SpatialQueryBuilder<String>
{
  private static Logger log = LoggerFactory.getLogger(JTSGeoQueryBuilder.class);

  public final JTSShapeIO reader;

  public JTSGeoQueryBuilder( JTSShapeIO reader )
  {
    this.reader = reader;
  }


  @Override
  public ValueSource makeValueSource(SpatialArgs args, String fname) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, String fname)
  {
    Geometry geo = reader.getGeometryFrom(args.shape);
    GeometryTest tester = GeometryTestFactory.get( args.op, geo );

    GeometryOperationFilter filter = new GeometryOperationFilter( fname, tester, reader.factory );
    return new FilteredQuery( new MatchAllDocsQuery(), filter );
  }

  @Override
  public Fieldable[] createFields(String fname, Shape shape,
      boolean index, boolean store )
  {
    Geometry geo = reader.getGeometryFrom(shape);
    String wkt = (store) ? geo.toText() : null;
    byte[] wkb = null;
    if( index ) {
      WKBWriter writer = new WKBWriter();
      wkb = writer.write( geo );

      if( wkb.length > 32000 ) {
        long last = wkb.length;
        Envelope env = geo.getEnvelopeInternal();
        double mins = Math.min( env.getWidth(), env.getHeight() );
        double div = 1000;
        while( true ) {
          double tolerance = mins/div;
          log.info( "Simplifying long geometry: WKB.length="+wkb.length+ " tolerance="+tolerance );
          Geometry simple = TopologyPreservingSimplifier.simplify( geo, tolerance );
          wkb = writer.write( simple );
          if( wkb.length < 32000 ) {
            break;
          }
          if( wkb.length == last ) {
            throw new InvalidShapeException( "Can not simplify geometry smaller then max. "+last );
          }
          last = wkb.length;
          div *= .70;
        }
      }
    }

    WKBField f = new WKBField(fname, wkb, wkt );
    return new Fieldable[] { f };
  }

}