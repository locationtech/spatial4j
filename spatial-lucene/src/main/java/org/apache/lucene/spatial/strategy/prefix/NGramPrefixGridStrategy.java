package org.apache.lucene.spatial.strategy.prefix;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.StringListTokenizer;
import org.apache.lucene.spatial.strategy.util.TruncateFilter;


public class NGramPrefixGridStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  protected final SpatialPrefixGrid grid;
  protected final int maxLength;

  public NGramPrefixGridStrategy(SpatialPrefixGrid grid, int maxLength) {
    this.grid = grid;
    this.maxLength = maxLength;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    List<String> cells = simplifyGridCells(SpatialPrefixGrid.cellsToTokenStrings(grid.getCells(shape)));
    BasicGridFieldable fieldable = new BasicGridFieldable(fieldInfo.getFieldName(), store);
    fieldable.tokens = new EdgeNGramTokenFilter(buildBasicTokenStream(cells), EdgeNGramTokenFilter.Side.FRONT, 1, 20);

    if (store) {
      fieldable.value = cells.toString();
    }

    return fieldable;
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo field) {
    return null;
  }


  @Override
  public Filter makeFilter(SpatialArgs args, SimpleSpatialFieldInfo field) {
    return new QueryWrapperFilter( makeQuery(args, field) );
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo field) {
    if (args.getOperation() != SpatialOperation.Intersects &&
        args.getOperation() != SpatialOperation.IsWithin &&
        args.getOperation() != SpatialOperation.Overlaps ){
      // TODO -- can translate these other query types
      throw new UnsupportedSpatialOperation(args.getOperation());
    }

    List<String> cells = simplifyGridCells(SpatialPrefixGrid.cellsToTokenStrings(grid.getCells(args.getShape())));

    BooleanQuery booleanQuery = new BooleanQuery();
    for (String cell : cells) {
      booleanQuery.add(new TermQuery(new Term(field.getFieldName(), cell)), BooleanClause.Occur.SHOULD);
    }
    return booleanQuery;
  }

  // ================================================= Helper Methods ================================================

  protected List<String> simplifyGridCells(List<String> gridCells) {
    List<String> newGridCells = new ArrayList<String>();
    for (String gridCell : gridCells) {
      newGridCells.add(gridCell.toLowerCase(Locale.ENGLISH).substring(0, gridCell.length() - 1));
    }
    return newGridCells;
  }

  protected TokenStream buildBasicTokenStream(List<String> gridCells) {
    if (maxLength > 0) {
      return new RemoveDuplicatesTokenFilter(
          new TruncateFilter(new StringListTokenizer(gridCells), maxLength));
    } else {
      return new StringListTokenizer(gridCells);
    }
  }
}
