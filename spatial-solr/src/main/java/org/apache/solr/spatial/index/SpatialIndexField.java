package org.apache.solr.spatial.index;
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
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SimpleShapeReader;
import org.apache.lucene.spatial.search.index.SpatialIndexProvider;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.apache.solr.search.QParser;


/**
 */
public class SpatialIndexField extends StrField
{
  SpatialIndexProvider provider = null;
  
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String v = args.remove( "doubleType" );
    
  }

  @Override
  public Fieldable createField(SchemaField field, Object val, float boost)
  {
    Shape shape = (val instanceof Shape)?((Shape)val):SimpleShapeReader.readSimpleShape( val.toString() );
    BBox bbox = shape.getBoundingBox();

    if( bbox.getCrossesDateLine() ) {
      throw new RuntimeException( this.getClass() + " does not support BBox crossing the date line" );
    }
    

    // within a single thread
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setMaximumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    nf.setMaximumIntegerDigits(3);
    nf.setMinimumIntegerDigits(3);
    
    String v = 
      nf.format( bbox.getMinX() ) + " " +
      nf.format( bbox.getMaxX() ) + " " +
      nf.format( bbox.getMinY() ) + " " +
      nf.format( bbox.getMaxY() );

    return createField(field.getName(), v, getFieldStore(field, v),
            getFieldIndex(field, v), getFieldTermVec(field, v), field.omitNorms(),
            field.omitTf(), boost);
  }

  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal)
  {
    // WITHIN( xmin xmax ymin ymax );
    System.out.println( "externalVal:"+externalVal );

    return new MatchAllDocsQuery();
  }

//  @Override
//  public ValueSource getValueSource(SchemaField field, QParser parser) {
//    return new StrFieldSource(field.name);
//  }

  @Override
  public boolean isPolyField() {
    return false;
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

