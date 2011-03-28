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

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.prefix.jts.JtsLinearPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.geohash.GeohashQueryBuilder;
import org.apache.lucene.spatial.search.geohash.GeohashSpatialIndexer;
import org.apache.lucene.spatial.search.geohash.GridReferenceSystem;
import org.apache.lucene.spatial.search.prefix.PrefixGridQueryBuilder;
import org.apache.lucene.spatial.search.prefix.PrefixGridSpatialIndexer;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;


/**
 * 
 */
public class GeohashFieldType extends SpatialFieldType<SimpleSpatialFieldInfo,GeohashSpatialIndexer,GeohashQueryBuilder> {

  public static final int DEFAULT_LENGTH = GridReferenceSystem.getMaxPrecision();//~12
  private GridReferenceSystem gridReferenceSystem;
  
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    
    // TODO, allow configuration
    reader = new JtsShapeIO();

    String len = args.remove("length");
    int maxLen = len!=null?Integer.parseInt(len): DEFAULT_LENGTH;
    gridReferenceSystem = new GridReferenceSystem( reader, maxLen );

    queryBuilder = new GeohashQueryBuilder( gridReferenceSystem );
    spatialIndexer = new GeohashSpatialIndexer( gridReferenceSystem );
    spatialIndexer.setIgnoreIncompatibleGeometry( true );
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}

