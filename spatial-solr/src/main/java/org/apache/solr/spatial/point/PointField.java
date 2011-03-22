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
/**
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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Point;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.point.PointQueryBuilder;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


public class PointField extends SpatialFieldType implements SchemaAware
{
  FieldType pointType;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    reader = new JTSShapeIO();
  }

  public void inform(IndexSchema schema)
  {
    pointType = schema.getFieldTypeByName( "double" );

    //Just set these, delegate everything else to the field type
    int p = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
    for( SchemaField sf : schema.getFields().values() ) {
      if( sf.getType() == this ) {
        String name = sf.getName();
        schema.getFields().put( name+PointQueryBuilder.SUFFIX_X, new SchemaField( name+PointQueryBuilder.SUFFIX_X, pointType, p, null ) );
        schema.getFields().put( name+PointQueryBuilder.SUFFIX_Y, new SchemaField( name+PointQueryBuilder.SUFFIX_Y, pointType, p, null ) );
      }
    }
  }

  @Override
  public boolean isPolyField(){
    return true;
  }

  @Override
  public Fieldable[] createFields(SchemaField field, Shape shape, float boost)
  {
    if( shape instanceof Point ) {
      Point point = (Point)shape;
      int p = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);
      Fieldable[] f = new Fieldable[field.stored()?3:2];
      f[0] = pointType.createField( new SchemaField( field.getName()+PointQueryBuilder.SUFFIX_X, pointType, p, null ), new Double( point.getX() ), boost);
      f[1] = pointType.createField( new SchemaField( field.getName()+PointQueryBuilder.SUFFIX_Y, pointType, p, null ), new Double( point.getY() ), boost);
      if( field.stored() ) {
        f[2] = new Field( field.getName(), reader.toString( shape ), Store.YES, Index.NO );
      }
      return f;
    }
    if( !ignoreIncompatibleGeometry ) {
      throw new IllegalArgumentException( "PointField does not support: "+shape );
    }
    return null;
  }

  @Override
  public Fieldable createField(SchemaField field, Shape value, float boost) {
    throw new UnsupportedOperationException( "this is a poly field");
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args )
  {
    PointQueryBuilder b = new PointQueryBuilder();
    return b.makeQuery(field.getName(), args);
  }
}

