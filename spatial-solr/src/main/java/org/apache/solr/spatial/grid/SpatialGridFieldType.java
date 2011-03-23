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

package org.apache.solr.spatial.grid;
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

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.grid.SpatialGrid;
import org.apache.lucene.spatial.base.grid.jts.JtsLinearSpatialGrid;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.grid.GridQueryBuilder;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.spatial.SpatialFieldType;


/**
 * Syntax for the field input:
 *
 * (1) QuadTokens: List of the fields it exists in:
 *    [ABA* CAA* AAAAAB-]
 *
 * (2) Something for the field reader....
 *
 */
public class SpatialGridFieldType extends SpatialFieldType
{
  int maxLength = -1;
  GridQueryBuilder builder;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String res = args.remove( "maxLength" );
    if( res != null ) {
      maxLength = Integer.parseInt( res );
    }

    // TODO, allow configuration
    reader = new JTSShapeIO();
    JtsLinearSpatialGrid grid = new JtsLinearSpatialGrid( -180, 180, -90-180, 90, 16 );
    grid.resolution = 5; // how far past the best fit to go

    this.init(grid, maxLength);
  }

  public void init( SpatialGrid grid, int maxLength )
  {
    this.maxLength = maxLength;
    this.builder = new GridQueryBuilder( grid );
  }

  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    return builder.makeField(field.getName(), shape, maxLength, field.stored() );
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args )
  {
    return builder.makeQuery(field.getName(), args);
  }
}

