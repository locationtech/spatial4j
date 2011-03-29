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

package org.apache.solr.spatial.prefix;

import java.util.Map;

import org.apache.lucene.spatial.base.prefix.jts.JtsLinearPrefixGrid;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.prefix.PrefixGridStrategy;
import org.apache.lucene.spatial.search.prefix.PrefixGridSpatialIndexer;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;


/**
 * Syntax for the field input:
 * <p/>
 * (1) QuadTokens: List of the fields it exists in:
 * [ABA* CAA* AAAAAB-]
 * <p/>
 * (2) Something for the field reader....
 */
public class SpatialPrefixGridFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    int maxLength = -1;
    String res = args.remove("maxLength");
    if (res != null) {
      maxLength = Integer.parseInt(res);
    }

    // TODO, allow configuration
    reader = new JtsShapeIO();
    JtsLinearPrefixGrid grid = new JtsLinearPrefixGrid(-180, 180, -90 - 180, 90, 16);
    grid.setResolution(5);

    spatialStrategy = new PrefixGridStrategy(grid, maxLength);
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}

