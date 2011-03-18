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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.RuntimeErrorException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.grid.jts.JtsLinearSpatialGrid;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaAware;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.SpatialQueryable;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SpatialOptions;


/**
 * Syntax for the field input:
 *
 * (1) QuadTokens: List of the fields it exists in:
 *    [ABA* CAA* AAAAAB-]
 *
 * (2) Point: X Y
 *   1.23 4.56
 *
 * (3) BOX: XMin YMin XMax YMax
 *   1.23 4.56 7.87 4.56
 *
 * (3) WKT
 *   POLYGON( ... )
 *
 */
public class SpatialGridField extends FieldType implements SchemaAware, SpatialQueryable
{
  // This is copied from Field type since they are private
  final static int INDEXED             = 0x00000001;
  final static int TOKENIZED           = 0x00000002;
  final static int STORED              = 0x00000004;
  final static int BINARY              = 0x00000008;
  final static int OMIT_NORMS          = 0x00000010;
  final static int OMIT_TF_POSITIONS   = 0x00000020;

  protected JtsLinearSpatialGrid grid;

  // Optionally copy a subset with maximum length
  protected FieldType cellType;
  protected String fprefix = null;
  protected byte[] resolutions = new byte[0];

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String res = args.remove( "resolutions" );
    if( res != null ) {
      StringTokenizer st = new StringTokenizer( res, ", " );
      resolutions = new byte[st.countTokens()];
      for( int i=0; i<resolutions.length; i++ ) {
        resolutions[i] = Byte.parseByte( st.nextToken() );
      }

      fprefix = args.remove( "prefix" );
      if( fprefix == null ) {
        throw new RuntimeException( "missing prefix field" );
      }
    }

    // TODO, allow configuration
    grid = new JtsLinearSpatialGrid( -180, 180, -90-180, 90, 16 );
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
  public Fieldable[] createFields(SchemaField field, Object val, float boost)
  {
    SimpleAbstractField[] fields = new SimpleAbstractField[1+resolutions.length];
    fields[0] = new SimpleAbstractField( field.getName(), field.stored() );

    Shape shape = null;
    if( val instanceof Shape ) {
      shape = (Shape)val;
    }
    else {
      String externalVal = val.toString();

      // The input *may* already define quad cells -- in this case, just use them
      if( externalVal.startsWith( "[" ) ) {
        // These are raw values... we will just reuse them
        fields[0].value = externalVal;
        fields[0].tokens = new GridCellsTokenizer( new StringReader( externalVal ) );
        for( int i=1; i<=resolutions.length; i++ ) {
          // Get the distinct shorter strings
          fields[i] = new SimpleAbstractField( fprefix+resolutions[i-1], false );
          fields[i].tokens = new RemoveDuplicatesTokenFilter(
              new TruncateFilter(
                  new GridCellsTokenizer( new StringReader( externalVal ) ), resolutions[i-1] ) );
        }
        return fields;
      }  
      try {
        shape = grid.readShape( externalVal );
      } 
      catch (IOException e) {
        throw new RuntimeException( "error reading: "+externalVal );
      }
    }
    
    List<CharSequence> match = grid.readCells(shape);
    if( field.stored() ) {
      // Store the cells, not the raw geometry
      fields[0].value = match.toString();
    }
    fields[0].tokens = new StringListTokenizer( match );
    for( int i=1; i<=resolutions.length; i++ ) {
      // Get the distinct shorter strings
      fields[i] = new SimpleAbstractField( fprefix+resolutions[i-1], false );
      fields[i].tokens = new RemoveDuplicatesTokenFilter(
          new TruncateFilter(
              new StringListTokenizer( match ), resolutions[i-1] ) );
    }
    return fields;
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
      Shape geo = grid.readShape( externalVal );
      List<CharSequence> match = grid.readCells(geo);

      BooleanQuery query = new BooleanQuery( true );
      for( CharSequence token : match ) {
        Term term = new Term( field.getName(), token.toString() );
        term.bytes().length--; // drop the last * or -
        query.add( new BooleanClause( new PrefixQuery( term ), BooleanClause.Occur.SHOULD  ) );
      }
      System.out.println( "QUERY: " + query );
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
}


class SimpleAbstractField extends AbstractField
{
  public String value;
  public TokenStream tokens;

  public SimpleAbstractField( String name, boolean stored ) {
    super( name, stored?Store.YES:Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO );
    setOmitTermFreqAndPositions( true );
  }

  @Override
  public Reader readerValue() {
    return new StringReader( value );
  }

  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public TokenStream tokenStreamValue() {
    return tokens;
  }
};

