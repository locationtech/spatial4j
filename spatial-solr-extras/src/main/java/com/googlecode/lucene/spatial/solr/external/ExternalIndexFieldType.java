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

package com.googlecode.lucene.spatial.solr.external;


import java.util.Map;

import com.googlecode.lucene.spatial.strategy.external.ExternalIndexStrategy;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;



/**
 * Field loads an in memory SpatialIndex (RTree or QuadTree)
 */
public class ExternalIndexFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    spatialStrategy = new ExternalIndexStrategy(reader);
    spatialStrategy.setIgnoreIncompatibleGeometry( ignoreIncompatibleGeometry );
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
