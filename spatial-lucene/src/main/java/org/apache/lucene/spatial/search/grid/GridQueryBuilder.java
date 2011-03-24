package org.apache.lucene.spatial.search.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.lucene.spatial.base.grid.SpatialGrid;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

public class GridQueryBuilder implements SpatialQueryBuilder<String>
{
  final SpatialGrid grid;
  final int maxLength;

  public GridQueryBuilder( SpatialGrid grid, int maxLength )
  {
    this.grid = grid;
    this.maxLength = maxLength;
  }

  @Override
  public Fieldable[] createFields(String fname, Shape shape,
      boolean index, boolean store )
  {
    List<CharSequence> match = grid.readCells(shape);
    BasicGridFieldable f = new BasicGridFieldable(fname, store );
    if( maxLength > 0 ) {
      f.tokens = new RemoveDuplicatesTokenFilter(
          new TruncateFilter( new StringListTokenizer( match ), maxLength ) );
    }
    else {
      f.tokens = new StringListTokenizer( match );
    }
    if( store ) {
      f.value = match.toString(); //reader.toString( shape );
    }
    return new Fieldable[] { f };
  }

  //-----------------------------------------------------
  //-----------------------------------------------------

  public ValueSource makeValueSource(SpatialArgs args, String fname)
  {
    throw new UnsupportedOperationException( "not implemented yet..." );
  }

  public Query makeQuery(SpatialArgs args, String fname)
  {
    if( args.op != SpatialOperation.Intersects &&
        args.op != SpatialOperation.IsWithin &&
        args.op != SpatialOperation.Overlaps &&
        args.op != SpatialOperation.SimilarTo ) {
      // TODO -- can translate these other query types
      throw new UnsupportedOperationException( "Unsupported Operation: "+args.op );
    }

    // TODO... resolution should help scoring...
    int resolution = grid.getBestLevel( args.shape );
    List<CharSequence> match = grid.readCells(args.shape);

    // TODO -- could this all happen in one pass?
    BooleanQuery query = new BooleanQuery( true );


    if( args.op == SpatialOperation.IsWithin ) {
      for( CharSequence token : match ) {
        Term term = new Term( fname, token.toString() );
        SpatialGridQuery q = new SpatialGridQuery( term );
        query.add( new BooleanClause( q, BooleanClause.Occur.SHOULD  ) );
      }
    }
    else {
      // Need to add all the parent queries
      Set<String> terms = new HashSet<String>();
      Set<String> parents = new HashSet<String>();
      for( CharSequence token : match ) {
        for( int i=1; i<token.length(); i++ ) {
          parents.add( token.subSequence(0, i)+"*" );
        }
        terms.add( token.toString().replace( '+', '*' ) );

        Term term = new Term( fname, token.toString() );
        SpatialGridQuery q = new SpatialGridQuery( term );
        query.add( new BooleanClause( q, BooleanClause.Occur.SHOULD  ) );
      }

      // These all include the '*'
      List<String> sorted = new ArrayList<String>( parents );
      Collections.sort( sorted );
      for( String t : sorted ) {
        if( !terms.contains( t ) ) {
          Term term = new Term( fname, t );
          PrefixQuery q = new PrefixQuery( term );
          query.add( new BooleanClause( q, BooleanClause.Occur.SHOULD  ) );
        }
      }
    }
    return query;
  }
}
