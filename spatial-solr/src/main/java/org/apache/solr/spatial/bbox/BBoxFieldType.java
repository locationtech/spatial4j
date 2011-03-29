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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.search.bbox.BBoxFieldInfo;
import org.apache.lucene.spatial.search.bbox.BBoxStrategy;
import org.apache.lucene.spatial.search.util.TrieFieldHelper;
import org.apache.solr.schema.BoolField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieDoubleField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.spatial.SpatialFieldType;


/**
 * Syntax for the field input:
 *
 */
public class BBoxFieldType extends SpatialFieldType<BBoxFieldInfo> implements SchemaAware
{
  protected String doubleFieldName = "double";
  protected String booleanFieldName = "boolean";

  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);

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

    spatialStrategy = new BBoxStrategy();
  }

  public void inform(IndexSchema schema)
  {
    FieldType doubleType = schema.getFieldTypeByName( doubleFieldName );
    FieldType booleanType = schema.getFieldTypeByName( booleanFieldName );

    if( doubleType == null ) {
      throw new RuntimeException( "Can not find double: "+doubleFieldName );
    }
    if( booleanType == null ) {
      throw new RuntimeException( "Can not find boolean: "+booleanFieldName );
    }
    if( !(booleanType instanceof BoolField) ) {
      throw new RuntimeException( "must be a booleanField: "+booleanType );
    }
    if( !(doubleType instanceof TrieDoubleField) ) {
      throw new RuntimeException( "double must be TrieDoubleField: "+doubleType );
    }

    BBoxStrategy strategy = (BBoxStrategy)spatialStrategy;
    TrieField df = (TrieField)doubleType;
    strategy.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    strategy.trieInfo.precisionStep = df.getPrecisionStep();
    strategy.trieInfo = new TrieFieldHelper.FieldInfo();
    strategy.trieInfo.store = true; // TODO properties &...
    strategy.trieInfo.index = true; // TODO properties &...

    List<SchemaField> fields = new ArrayList<SchemaField>( schema.getFields().values() );
    for( SchemaField sf : fields ) {
      if( sf.getType() == this ) {
        BBoxFieldInfo info = getFieldInfo(sf);
        register( schema, new SchemaField( info.minX, doubleType, fieldProps, null ) );
        register( schema, new SchemaField( info.maxX, doubleType, fieldProps, null ) );
        register( schema, new SchemaField( info.minY, doubleType, fieldProps, null ) );
        register( schema, new SchemaField( info.maxY, doubleType, fieldProps, null ) );
        register( schema, new SchemaField( info.xdl, booleanType, fieldProps, null ) );
      }
    }
  }

  private void register( IndexSchema s, SchemaField sf )
  {
    s.getFields().put( sf.getName(), sf );
  }


  @Override
  protected BBoxFieldInfo getFieldInfo(SchemaField field) {
    BBoxFieldInfo info = new BBoxFieldInfo();
    info.setFieldsPrefix( field.getName() );
    return info;
  }
}

