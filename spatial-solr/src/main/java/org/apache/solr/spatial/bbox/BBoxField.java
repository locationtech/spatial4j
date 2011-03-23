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
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.bbox.BBoxFieldInfo;
import org.apache.lucene.spatial.search.bbox.BBoxQueryBuilder;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


/**
 * Syntax for the field input:
 *
 */
public class BBoxField extends SpatialFieldType implements SchemaAware
{
  protected String doubleFieldName = "double";
  protected String booleanFieldName = "boolean";

  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
  protected FieldType doubleType;
  protected FieldType booleanType;

  double queryPower = 1.0;
  double targetPower = 1.0f;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    reader = new JTSShapeIO();

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
      throw new RuntimeException( "Can not find boolean: "+booleanFieldName );
    }

    //Just set these, delegate everything else to the field type
    schema.registerDynamicField( new SchemaField( "*"+BBoxFieldInfo.SUFFIX_MINX, doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*"+BBoxFieldInfo.SUFFIX_MINY, doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*"+BBoxFieldInfo.SUFFIX_MAXX, doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*"+BBoxFieldInfo.SUFFIX_MAXY, doubleType, fieldProps, null ) );
    schema.registerDynamicField( new SchemaField( "*"+BBoxFieldInfo.SUFFIX_XDL, booleanType, fieldProps, null ) );
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

    int fp = fieldProps | STORED;  // useful for debugging

    Fieldable[] fields = new Fieldable[5];
    fields[0] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MINX, doubleType, fp, null ).createField( new Double(bbox.getMinX()), 1.0f);
    fields[1] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MAXX, doubleType, fp, null ).createField( new Double(bbox.getMaxX()), 1.0f);
    fields[2] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MINY, doubleType, fp, null ).createField( new Double(bbox.getMinY()), 1.0f);
    fields[3] = new SchemaField( name+BBoxFieldInfo.SUFFIX_MAXY, doubleType, fp, null ).createField( new Double(bbox.getMaxY()), 1.0f);
    fields[4] = new SchemaField( name+BBoxFieldInfo.SUFFIX_XDL, booleanType, fp, null ).createField( new Boolean(bbox.getCrossesDateLine()), 1.0f);
    return fields;
  }

  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    BBoxQueryBuilder builder = new BBoxQueryBuilder();
    BBoxFieldInfo info = new BBoxFieldInfo();
    info.setFieldsPrefix( field.getName() );
    // TODO make sure the parser and precision step are set!
    // TODO make sure the parser and precision step are set!
    info.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    info.precisionStep = Integer.MAX_VALUE; // set to zero
    return builder.makeQuery(info, args);
  }

  @Override
  public boolean isPolyField() {
    return true;
  }
}

