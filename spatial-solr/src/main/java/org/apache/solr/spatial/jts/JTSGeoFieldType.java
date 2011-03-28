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

import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.jts.JtsGeoQueryBuilder;
import org.apache.lucene.spatial.search.jts.JtsGeoSpatialIndexer;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;

import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Indexed field is WKB (store WKT)
 * <p/>
 * Maximum bytes for WKB is 32000, this will simplify geometry till there are fewer then 32K bytes
 */
public class JTSGeoFieldType extends SpatialFieldType<SimpleSpatialFieldInfo,JtsGeoSpatialIndexer,JtsGeoQueryBuilder> {

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    GeometryFactory factory = ((JtsShapeIO)reader).factory;
    queryBuilder = new JtsGeoQueryBuilder(factory);
    spatialIndexer = new JtsGeoSpatialIndexer(factory);
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
