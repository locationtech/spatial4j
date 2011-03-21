package org.apache.solr.spatial.grid;
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

import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.GeometryArgs;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.WithinDistanceArgs;
import org.apache.lucene.spatial.base.grid.jts.JtsLinearSpatialGrid;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.grid.SpatialGridQuery;
import org.apache.solr.common.SolrException;
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
public class SpatialGridField extends SpatialFieldType
{
  protected JtsLinearSpatialGrid grid;
  protected int resolution = -1;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String res = args.remove( "resolution" );
    if( res != null ) {
      resolution = Integer.parseInt( res );
    }

    // TODO, allow configuration
    reader = new JTSShapeIO();
    grid = new JtsLinearSpatialGrid( -180, 180, -90-180, 90, 16 );
    grid.resolution = 5; // how far past the best fit to go
  }


  @Override
  public Fieldable createField(SchemaField field, Shape shape, float boost)
  {
    List<CharSequence> match = grid.readCells(shape);
    BasicGridFieldable f = new BasicGridFieldable(field.getName(), field.stored());
    if( resolution > 0 ) {
      f.tokens = new RemoveDuplicatesTokenFilter(
          new TruncateFilter( new StringListTokenizer( match ), resolution ) );
    }
    else {
      f.tokens = new StringListTokenizer( match );
    }
    if( field.stored() ) {
      f.value = match.toString(); //reader.toString( shape );
    }
    return f;
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args )
  {
    if( args instanceof WithinDistanceArgs ) {
      throw new UnsupportedOperationException( "distance calculation is not yet supported" );
    }
    GeometryArgs g = (GeometryArgs)args;

    // assume 'mostly within' query
    try {
      List<CharSequence> match = grid.readCells(g.shape);

      // TODO -- could optimize to use the right resolutions
      BooleanQuery query = new BooleanQuery( true );
      for( CharSequence token : match ) {
        Term term = new Term( field.getName(), token.toString() );
        query.add( new BooleanClause(
            new SpatialGridQuery( term ), BooleanClause.Occur.SHOULD  ) );
      }
      System.out.println( "QUERY: " + query );
      return query;
    }
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, e );
    }
  }
}

