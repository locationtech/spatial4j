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

package org.apache.solr.spatial.geohash;

import java.util.Map;

import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.geohash.GeohashStrategy;
import org.apache.lucene.spatial.strategy.geohash.GridReferenceSystem;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;


/**
 *
 */
public class GeohashFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

  public static final int DEFAULT_LENGTH = GridReferenceSystem.getMaxPrecision();//~12
  private GridReferenceSystem gridReferenceSystem;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String len = args.remove("length");
    int maxLen = len!=null?Integer.parseInt(len): DEFAULT_LENGTH;
    gridReferenceSystem = new GridReferenceSystem( reader, maxLen );
    spatialStrategy = new GeohashStrategy( gridReferenceSystem );
    spatialStrategy.setIgnoreIncompatibleGeometry( ignoreIncompatibleGeometry );
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}

