package org.apache.lucene.spatial.search.grid;

import java.util.List;

import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.lucene.spatial.base.grid.SpatialGrid;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

public class GridQueryBuilder extends SpatialQueryBuilder
{
  final SpatialGrid grid;

  public GridQueryBuilder( SpatialGrid grid )
  {
    this.grid = grid;
  }

  public Fieldable makeField( String fname, Shape shape, int maxLength, boolean stored )
  {
    List<CharSequence> match = grid.readCells(shape);
    BasicGridFieldable f = new BasicGridFieldable(fname, stored);
    if( maxLength > 0 ) {
      f.tokens = new RemoveDuplicatesTokenFilter(
          new TruncateFilter( new StringListTokenizer( match ), maxLength ) );
    }
    else {
      f.tokens = new StringListTokenizer( match );
    }
    if( stored ) {
      f.value = match.toString(); //reader.toString( shape );
    }
    return f;
  }

  //-----------------------------------------------------
  //-----------------------------------------------------

  @Override
  public ValueSource makeValueSource(String fname, SpatialArgs args)
  {
    throw new UnsupportedOperationException( "not implemented yet..." );
  }

  @Override
  public Query makeQuery(String fname, SpatialArgs args)
  {
    if( args.op != SpatialOperation.BBoxIntersects ||
        args.op != SpatialOperation.Intersects ||
        args.op != SpatialOperation.Overlaps ||
        args.op != SpatialOperation.SimilarTo ) {
      // TODO -- can translate these other query types
      throw new UnsupportedOperationException( "Unsupported Operation: "+args.op );
    }

    // assume 'mostly within' query
    List<CharSequence> match = grid.readCells(args.shape);

    // TODO -- could this all happen in one pass?
    BooleanQuery query = new BooleanQuery( true );
    for( CharSequence token : match ) {
      Term term = new Term( fname, token.toString() );
      query.add( new BooleanClause(
          new SpatialGridQuery( term ), BooleanClause.Occur.SHOULD  ) );
    }
    return query;
  }
}
