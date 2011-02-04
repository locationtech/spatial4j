package voyager.quads.solr;
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
import java.util.ArrayList;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.SpatialQueryable;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SpatialOptions;

import voyager.quads.LevelMatchInfo;
import voyager.quads.MatchInfo;
import voyager.quads.SpatialGrid;
import voyager.quads.geometry.Shape;



public class QuadTreeFieldOld extends FieldType implements SchemaAware, SpatialQueryable
{
  // This is copied from Field type since they are private
  final static int INDEXED             = 0x00000001;
  final static int TOKENIZED           = 0x00000002;
  final static int STORED              = 0x00000004;
  final static int BINARY              = 0x00000008;
  final static int OMIT_NORMS          = 0x00000010;
  final static int OMIT_TF_POSITIONS   = 0x00000020;

  protected String fprefix;
  protected SpatialGrid grid;
  protected FieldType cellType;
  protected IndexSchema schema;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    this.schema = schema;

    fprefix = args.remove( "prefix" );
    if( fprefix == null ) {
      throw new RuntimeException( "missing prefix field" );
    }
    grid = new SpatialGrid( -180, 180, -90-180, 90, 16 );
    grid.resolution = 5; // how far past the best fit to go
  }

  public void inform(IndexSchema schema) {

    cellType = schema.getFieldTypeByName( "string" );

    //Just set these, delegate everything else to the field type

    //Just set these, delegate everything else to the field type
    int p = (INDEXED | TOKENIZED | OMIT_NORMS | OMIT_TF_POSITIONS);

    SchemaField sf = new SchemaField(fprefix+"*", cellType, p, null );
    schema.registerDynamicField( sf );
  }

  @Override
  public Fieldable[] createFields(SchemaField field, String externalVal, float boost)
  {
    ArrayList<Fieldable> fields = new ArrayList<Fieldable>();
    if( field.stored() ) {
      fields.add( createField(field.getName(), externalVal,
          getFieldStore(field, externalVal), Field.Index.NO, Field.TermVector.NO,
          false, false, boost));
    }

    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setMinimumIntegerDigits( 2 );

    try {
      Shape geo = Shape.parse( externalVal );
      MatchInfo match = grid.read(geo);

      for( LevelMatchInfo level : match.levels ) {
        String pfix = fprefix + nf.format( level.level );

        if( !level.intersects.isEmpty() ) {
          fields.add( new Field( pfix, new StringListTokenizer( level.intersects ), TermVector.NO ) );
        }
        level.covers.addAll( level.depth ); // TODO?  distinction? new field?
        if( !level.covers.isEmpty() ) {
          fields.add( new Field( pfix+"_cover", new StringListTokenizer( level.covers ), TermVector.NO ) );
        }
      }
      return fields.toArray( new Fieldable[fields.size()] );
    }
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, e );
    }
  }


  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal)
  {
    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setMinimumIntegerDigits( 2 );

    // assume 'mostly within' query
    try {
      Shape geo = Shape.parse( externalVal );
      MatchInfo match = grid.read(geo);
      MatchInfo qmatch = MatchInfo.getMostlyWithinQueryTokens( match.tokens );
      int bestfit = match.bboxLevel; // use as boost
      int depth = match.levels.size();

      BooleanQuery query = new BooleanQuery( true );
      for( LevelMatchInfo level : qmatch.levels ) {
        String fname = fprefix+nf.format( level.level );

        if( level.intersects.size() > 0 ) {
          FieldValuesFilter filter = new FieldValuesFilter( fname, level.intersects );
          Query q = new FilteredQuery( new MatchAllDocsQuery(), filter );
          query.add( new BooleanClause( q, BooleanClause.Occur.SHOULD  ) );
        }
        if( level.covers.size() > 0 ) {
          FieldValuesFilter filter = new FieldValuesFilter( fname+"_cover", level.covers );
          Query q = new FilteredQuery( new MatchAllDocsQuery(), filter );
          q.setBoost( (depth - level.level) + 2 ); // TODO.. use bestfit???
          query.add( new BooleanClause( q, BooleanClause.Occur.SHOULD  ) );
        }
      }

      System.out.println( "QUERY: " + query );

      //CustomScoreQuery q = new CustomScoreQuery()
      return query;
    }
    catch (Exception e) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, e );
    }
  }

  @Override
  public Query createSpatialQuery(QParser parser, SpatialOptions options) {
    throw new UnsupportedOperationException();
  }

//  @Override
//  public ValueSource getValueSource(SchemaField field, QParser parser) {
//    return new StrFieldSource(field.name);
//  }

  @Override
  public boolean isPolyField() {
    return true;
  }

  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeStr(name, f.stringValue(), false);
  }

  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on QuadTreeField " + field.getName());
  }

  @Override
  public Field createField(SchemaField field, String externalVal, float boost) {
    throw new UnsupportedOperationException("QuadField needs multiple fields.  field=" + field.getName());
  }
}



