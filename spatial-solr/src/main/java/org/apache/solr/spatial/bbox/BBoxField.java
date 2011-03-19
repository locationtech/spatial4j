package org.apache.solr.spatial.bbox;

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

import java.util.Map;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.search.bbox.BBoxFieldInfo;
import org.apache.lucene.spatial.search.bbox.BBoxQueryBuilder;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


import org.apache.lucene.search.BooleanClause;


/**
 * Syntax for the field input:
 *
 * (1) QuadTokens: List of the fields it exists in:
 *    [ABA* CAA* AAAAAB-]
 *
 */
public class BBoxField extends SpatialFieldType implements SchemaAware
{
  // This is copied from Field type since they are private
  final static int INDEXED             = 0x00000001;
  final static int TOKENIZED           = 0x00000002;
  final static int STORED              = 0x00000004;
  final static int BINARY              = 0x00000008;
  final static int OMIT_NORMS          = 0x00000010;
  final static int OMIT_TF_POSITIONS   = 0x00000020;

  protected String doubleFieldName = "double";
  protected String booleanFieldName = "bool";

  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
  protected FieldType doubleType;
  protected FieldType booleanType;

  double queryPower = 1.0;
  double targetPower = 1.0f;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String v = args.remove( "doubleType" );
    if( v != null ) {
      doubleFieldName = v;
    }

    v = args.remove( "booleanType" );
    if( v != null ) {
      booleanFieldName = v;
    }
  }

  public void inform(IndexSchema schema)
  {
    doubleType = schema.getFieldTypeByName( doubleFieldName );
    booleanType = schema.getFieldTypeByName( booleanFieldName );

    if( doubleType == null ) {
      throw new RuntimeException( "Can not find double: "+doubleFieldName );
    }

    if( booleanType == null ) {
      throw new RuntimeException( "Can not find boolean: "+booleanType );
    }

    //Just set these, delegate everything else to the field type
    schema.registerDynamicField( new SchemaField( "*__minX", doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*__minY", doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*__maxX", doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*__maxY", doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*__xxdl", booleanType, fieldProps, null ) );
  }

  @Override
  public Fieldable createField(SchemaField field, Shape value, float boost) {
    throw new UnsupportedOperationException( "this is a multivalued field" );
  }

  @Override
  public Fieldable[] createFields(SchemaField field, Shape shape, float boost)
  {
    BBox bbox = shape.getBoundingBox();
    
    String name = field.getName();

    Fieldable[] fields = new Fieldable[5];
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MINX, doubleType, fieldProps, null ).createField( new Double(bbox.getMinX()), 1.0f);
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MAXX, doubleType, fieldProps, null ).createField( new Double(bbox.getMaxX()), 1.0f);
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MINY, doubleType, fieldProps, null ).createField( new Double(bbox.getMinY()), 1.0f);
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MAXY, doubleType, fieldProps, null ).createField( new Double(bbox.getMaxY()), 1.0f);
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_XDL, booleanType, fieldProps, null ).createField( new Boolean(bbox.getCrossesDateLine()), 1.0f);
    return fields;
  }

  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    BBoxFieldInfo fields = new BBoxFieldInfo();
    fields.setFieldsPrefix( field.getName() );
    BBoxQueryBuilder builder = new BBoxQueryBuilder();
    Query query = builder.getQuery(args.op);
    if( args.calculateScore ) {
      Query spatialRankingQuery = new ValueSourceQuery( builder.makeValueSource( args.op ) );
      BooleanQuery bq = new BooleanQuery();
      bq.add(query,BooleanClause.Occur.MUST);
      bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
      return bq;
    }
    return query;
  }

//  @Override
//  public ValueSource getValueSource(SchemaField field, QParser parser) {
//    return new StrFieldSource(field.name);
//  }

  @Override
  public boolean isPolyField() {
    return true;
  }
}

