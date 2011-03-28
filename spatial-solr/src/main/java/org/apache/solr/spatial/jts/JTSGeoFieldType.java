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

package org.apache.solr.spatial.jts;

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

import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.jts.JTSGeoQueryBuilder;
import org.apache.lucene.spatial.search.jts.JTSGeoSpatialIndexer;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Indexed field is WKB (store WKT)
 * <p/>
 * Maximum bytes for WKB is 32000, this will simplify geometry till there are fewer then 32K bytes
 */
public class JTSGeoFieldType extends SpatialFieldType {
  
  static final Logger log = LoggerFactory.getLogger(JTSGeoFieldType.class);
  JTSGeoQueryBuilder builder;
  JTSGeoSpatialIndexer spatialIndexer;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    GeometryFactory factory = new GeometryFactory();
    builder = new JTSGeoQueryBuilder(factory);
    reader = new JTSShapeIO(factory);
    spatialIndexer = new JTSGeoSpatialIndexer(factory);
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost) {
    return spatialIndexer.createFields(new SimpleSpatialFieldInfo(field.getName()), shape,
        field.indexed(), field.stored())[0];
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args) {
    return builder.makeQuery(args, new SimpleSpatialFieldInfo(field.getName()));
  }
}
