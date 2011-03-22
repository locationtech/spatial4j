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

package org.apache.solr.spatial.index;
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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.index.STRTreeIndexProvider;
import org.apache.lucene.spatial.search.index.SpatialIndexFilter;
import org.apache.lucene.spatial.search.index.SpatialIndexProvider;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


/**
 */
public class SpatialIndexField extends SpatialFieldType
{
  final Map<String, SpatialIndexProvider> provider
    = new ConcurrentHashMap<String, SpatialIndexProvider>();

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    reader = new JTSShapeIO();
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    BBox bbox = shape.getBoundingBox();
    if( bbox.getCrossesDateLine() ) {
      throw new RuntimeException( this.getClass() + " does not support BBox crossing the date line" );
    }
    String v = reader.toString( bbox );
    return createField(field.getName(), v, getFieldStore(field, v),
            getFieldIndex(field, v), getFieldTermVec(field, v), field.omitNorms(),
            field.omitTf(), boost);
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args)
  {
    if( args.shape.getBoundingBox().getCrossesDateLine() ) {
      throw new UnsupportedOperationException( "Spatial Index does not (yet) support queries that cross the date line" );
    }

    SpatialIndexProvider p = provider.get( field.getName() );
    if( p == null ) {
      p = new STRTreeIndexProvider( 30, field.getName(), reader );
      provider.put( field.getName(), p );
    }

    // just a filter wrapper for now...
    SpatialIndexFilter filter = new SpatialIndexFilter( p, args );
    return new FilteredQuery( new MatchAllDocsQuery(), filter );
  }
}
