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

package org.apache.solr.spatial.point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.search.point.PointFieldInfo;
import org.apache.lucene.spatial.search.point.PointStrategy;
import org.apache.lucene.spatial.search.util.TrieFieldHelper;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TrieDoubleField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.spatial.SpatialFieldType;


public class PointFieldType extends SpatialFieldType<PointFieldInfo> implements SchemaAware
{
  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);

  protected String doubleFieldName = "double";

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String v = args.remove( "doubleType" );
    if( v != null ) {
      doubleFieldName = v;
    }
  }

  public void inform(IndexSchema schema)
  {
    FieldType doubleType = schema.getFieldTypeByName( doubleFieldName );
    if( doubleType == null ) {
      throw new RuntimeException( "Can not find double: "+doubleFieldName );
    }
    if( !(doubleType instanceof TrieDoubleField) ) {
      throw new RuntimeException( "double must be TrieDoubleField: "+doubleType );
    }

    //Just set these, delegate everything else to the field type
    int p = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
    List<SchemaField> fields = new ArrayList<SchemaField>( schema.getFields().values() );
    for( SchemaField sf : fields ) {
      if( sf.getType() == this ) {
        String name = sf.getName();
        schema.getFields().put( name+PointFieldInfo.SUFFIX_X, new SchemaField( name+PointFieldInfo.SUFFIX_X, doubleType, p, null ) );
        schema.getFields().put( name+PointFieldInfo.SUFFIX_Y, new SchemaField( name+PointFieldInfo.SUFFIX_Y, doubleType, p, null ) );
      }
    }

    TrieField df = (TrieField)doubleType;
    TrieFieldHelper.FieldInfo info = new TrieFieldHelper.FieldInfo();
    info.precisionStep = df.getPrecisionStep();
    info.store = true; // TODO properties &...

    spatialStrategy = new PointStrategy(reader,info,FieldCache.NUMERIC_UTILS_DOUBLE_PARSER);
  }

  @Override
  protected PointFieldInfo getFieldInfo(SchemaField field) {
    return new PointFieldInfo(field.getName());
  }
}

