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

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.core.BBox;
import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.core.SimpleShapeReader;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;


/**
 * Syntax for the field input:
 *
 * (1) QuadTokens: List of the fields it exists in:
 *    [ABA* CAA* AAAAAB-]
 *
 * (2) Point: X Y
 *   1.23 4.56
 *
 * (3) BOX: XMin YMin XMax YMax
 *   1.23 4.56 7.87 4.56
 *
 * (3) WKT
 *   POLYGON( ... )
 *
 */
public class BBoxField extends FieldType implements SchemaAware
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
  public Fieldable[] createFields(SchemaField field, Object val, float boost)
  {
    Shape shape = (val instanceof Shape)?((Shape)val):SimpleShapeReader.readSimpleShape( val.toString() );
    BBox bbox = shape.getBoundingBox();

    String name = field.getName();

    Fieldable[] fields = new Fieldable[5];
    fields[0] = new SchemaField( name+"__minX", doubleType, fieldProps, null ).createField( String.valueOf(bbox.getMinX()), 1.0f);
    fields[0] = new SchemaField( name+"__maxX", doubleType, fieldProps, null ).createField( String.valueOf(bbox.getMinY()), 1.0f);
    fields[0] = new SchemaField( name+"__minY", doubleType, fieldProps, null ).createField( String.valueOf(bbox.getMaxX()), 1.0f);
    fields[0] = new SchemaField( name+"__maxY", doubleType, fieldProps, null ).createField( String.valueOf(bbox.getMaxY()), 1.0f);
    fields[0] = new SchemaField( name+"__xxdl", doubleType, fieldProps, null ).createField( String.valueOf(bbox.getCrossesDateLine()), 1.0f);
    return fields;
  }

  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal)
  {
    System.out.println( "externalVal:"+externalVal );

    return new MatchAllDocsQuery();
  }

//  @Override
//  public ValueSource getValueSource(SchemaField field, QParser parser) {
//    return new StrFieldSource(field.name);
//  }

  @Override
  public boolean isPolyField() {
    return true;
  }

  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeStr(name, f.stringValue(), false);
  }

  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on QuadTreeField " + field.getName());
  }
}

