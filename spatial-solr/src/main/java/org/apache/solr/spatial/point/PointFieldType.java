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
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


public class PointFieldType extends SpatialFieldType implements SchemaAware
{
  protected final int fieldProps = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);

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
  public boolean isPolyField(){
    return true;
  }

  @Override
  public Fieldable[] createFields(SchemaField field, Shape shape, float boost)
  {
    if( shape instanceof Point ) {
      Point point = (Point)shape;
      int p = fieldProps | STORED;  // useful for debugging

      Fieldable[] f = new Fieldable[field.stored()?3:2];
      f[0] = pointType.createField( new SchemaField( field.getName()+PointFieldInfo.SUFFIX_X, pointType, p, null ), new Double( point.getX() ), boost);
      f[1] = pointType.createField( new SchemaField( field.getName()+PointFieldInfo.SUFFIX_Y, pointType, p, null ), new Double( point.getY() ), boost);
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
    PointFieldInfo info = new PointFieldInfo();
    info.setFieldsPrefix( field.getName() );
    // TODO make sure the parser and precision step are set!
    info.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    info.precisionStep = Integer.MAX_VALUE; // set to zero
    return b.makeQuery(args, info);
  }
}

