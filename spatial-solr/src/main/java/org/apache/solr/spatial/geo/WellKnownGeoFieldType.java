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

package org.apache.solr.spatial.geo;
/**
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

import java.util.Map;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.geo.GeometryOperationFilter;
import org.apache.lucene.spatial.search.geo.WellKnownGeoField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;


/**
 * Indexed field is WKB (can we store WKT??)
 */
public class WellKnownGeoFieldType extends SpatialFieldType
{
  static final Logger log = LoggerFactory.getLogger( WellKnownGeoFieldType.class );


  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    reader = new JTSShapeIO();
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    Geometry geo = ((JTSShapeIO)reader).getGeometryFrom(shape);
    String wkt = field.stored() ? geo.toText() : null;
    byte[] wkb = null;
    if( field.indexed() ) {
      WKBWriter writer = new WKBWriter();
      wkb = writer.write( geo );
    }

    WellKnownGeoField f = new WellKnownGeoField(field.getName(), wkb, wkt );
    f.setBoost(boost);
    return f;
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    GeometryOperationFilter filter = new GeometryOperationFilter( field.getName(), args, ((JTSShapeIO)reader) );
    return new FilteredQuery( new MatchAllDocsQuery(), filter );
  }
}
