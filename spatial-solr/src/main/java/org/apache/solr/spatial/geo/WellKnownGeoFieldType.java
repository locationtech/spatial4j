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
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.geo.GeoFieldInfo;
import org.apache.lucene.spatial.search.geo.GeoQueryBuilder;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Indexed field is WKB (store WKT)
 *
 * Maximum bytes for WKB is 3200, this will simplify geometry till there are fewer then 32K bytes
 *
 */
public class WellKnownGeoFieldType extends SpatialFieldType
{
  static final Logger log = LoggerFactory.getLogger( WellKnownGeoFieldType.class );
  GeoQueryBuilder builder;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    builder = new GeoQueryBuilder( new JTSShapeIO() );
    reader = builder.reader;
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    GeoFieldInfo info = new GeoFieldInfo( field.getName() );
    return builder.createFields(info, shape,
        field.indexed(), field.stored() )[0];
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    GeoFieldInfo info = new GeoFieldInfo( field.getName() );
    return builder.makeQuery(args, info);
  }
}
