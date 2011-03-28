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

package org.apache.solr.spatial;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialIndexer;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class SpatialFieldType<T extends SpatialFieldInfo, I extends SpatialIndexer<T>, Q extends SpatialQueryBuilder<T>> extends FieldType
{
  // This is copied from Field type since they are private
  protected final static int INDEXED             = 0x00000001;
  protected final static int TOKENIZED           = 0x00000002;
  protected final static int STORED              = 0x00000004;
  protected final static int BINARY              = 0x00000008;
  protected final static int OMIT_NORMS          = 0x00000010;
  protected final static int OMIT_TF_POSITIONS   = 0x00000020;

  static final Logger log = LoggerFactory.getLogger( SpatialFieldType.class );
  
  protected ShapeIO reader;
  protected SpatialArgsParser argsParser;
  protected boolean ignoreIncompatibleGeometry = true;
  
  protected I spatialIndexer;
  protected Q queryBuilder;
  
  
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    // TODO, read configuration from Map
    reader = new JTSShapeIO();  // some way to share this across different fields?
    argsParser = new SpatialArgsParser();
    ignoreIncompatibleGeometry = true;
  }
  
  protected abstract T getFieldInfo( SchemaField field );

  @Override
  public Fieldable createField(SchemaField field, Object val, float boost)
  {
    Shape shape = (val instanceof Shape)?((Shape)val):reader.readShape( val.toString() );
    if( shape == null ) {
      log.warn( "null shape for input: "+val );
      return null;
    }
    return createField(field, shape, boost);
  }

  @Override
  public Fieldable[] createFields(SchemaField field, Object val, float boost)
  {
    Shape shape = (val instanceof Shape)?((Shape)val):reader.readShape( val.toString() );
    return createFields(field, shape, boost);
  }

  public abstract Fieldable createField(SchemaField field, Shape value, float boost);

  public Fieldable[] createFields(SchemaField field, Shape value, float boost) {
    Fieldable f = createField( field, value, boost);
    return f==null ? new Fieldable[]{} : new Fieldable[]{f};
  }


  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final Query getFieldQuery(QParser parser, SchemaField field, String externalVal)
  {
    return getFieldQuery( parser, field, argsParser.parse( externalVal, reader ) );
  }

  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    return queryBuilder.makeQuery(args, getFieldInfo(field));
  }

  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    if( f.isBinary() ) {
      byte[] bytes = f.getBinaryValue();
      Shape s = reader.readShape( bytes, 0, bytes.length );
      writer.writeStr(name, reader.toString(s), true);
    }
    else {
      writer.writeStr(name, f.stringValue(), true);
    }
  }

  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on SpatialField: " + field.getName());
  }
}


