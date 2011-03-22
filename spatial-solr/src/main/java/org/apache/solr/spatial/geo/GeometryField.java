package org.apache.solr.spatial.geo;
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

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.geo.GeometryOperationFilter;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Indexed field is WKB (can we store WKT??)
 */
public class GeometryField extends SpatialFieldType
{
  static final Logger log = LoggerFactory.getLogger( GeometryField.class );

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    reader = new JTSShapeIO();
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    if (!field.stored()) {
      log.trace("Ignoring unstored binary field: " + field);
      return null;
    }

    try {
      byte[] bytes = reader.toBytes( shape );
      Field f = new Field(field.getName(), bytes, 0, bytes.length);
      f.setBoost(boost);
      return f;
    }
    catch( IOException ex ) {
      throw new RuntimeException("bad shape", ex);
    }
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    GeometryOperationFilter filter = new GeometryOperationFilter( field.getName(), args, reader );
    return new FilteredQuery( new MatchAllDocsQuery(), filter );
  }
}
