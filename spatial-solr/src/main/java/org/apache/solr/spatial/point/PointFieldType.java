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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.point.PointFieldInfo;
import org.apache.lucene.spatial.search.point.PointQueryBuilder;
import org.apache.lucene.spatial.search.point.PointSpatialIndexer;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


public class PointFieldType extends SpatialFieldType<PointFieldInfo,PointSpatialIndexer,PointQueryBuilder> implements SchemaAware
{
  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);

  FieldType pointType;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    
    spatialIndexer = new PointSpatialIndexer() {
      @Override
      public Fieldable[] createFields(PointFieldInfo fieldInfo,
          Shape shape, boolean index, boolean store) {
        if( shape instanceof Point ) {
          Point point = (Point)shape;
          int p = fieldProps | STORED;  // useful for debugging

          Fieldable[] f = new Fieldable[store?3:2];
          f[0] = pointType.createField( new SchemaField( fieldInfo.getXFieldName(), pointType, p, null ), new Double( point.getX() ), 1.0f );
          f[1] = pointType.createField( new SchemaField( fieldInfo.getYFieldName(), pointType, p, null ), new Double( point.getY() ), 1.0f );
          if( store ) {
            f[2] = new Field( fieldInfo.getFieldName(), reader.toString( shape ), Store.YES, Index.NO );
          }
          return f;
        }
        if( !ignoreIncompatibleGeometry ) {
          throw new IllegalArgumentException( "PointField does not support: "+shape );
        }
        return null;
      }
    };
  }

  public void inform(IndexSchema schema)
  {
    pointType = schema.getFieldTypeByName( "double" );

    //Just set these, delegate everything else to the field type
    int p = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
    List<SchemaField> fields = new ArrayList<SchemaField>( schema.getFields().values() );
    for( SchemaField sf : fields ) {
      if( sf.getType() == this ) {
        String name = sf.getName();
        schema.getFields().put( name+PointFieldInfo.SUFFIX_X, new SchemaField( name+PointFieldInfo.SUFFIX_X, pointType, p, null ) );
        schema.getFields().put( name+PointFieldInfo.SUFFIX_Y, new SchemaField( name+PointFieldInfo.SUFFIX_Y, pointType, p, null ) );
      }
    }
  }

  @Override
  protected PointFieldInfo getFieldInfo(SchemaField field) {
    return new PointFieldInfo(field.getName(), Integer.MAX_VALUE, FieldCache.NUMERIC_UTILS_DOUBLE_PARSER);
  }
}

