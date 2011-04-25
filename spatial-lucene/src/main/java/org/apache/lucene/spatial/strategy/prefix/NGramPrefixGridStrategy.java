package org.apache.lucene.spatial.strategy.prefix;

import java.util.List;

import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;

/**
 * @author Chris Male
 */
public class NGramPrefixGridStrategy extends PrefixGridStrategy {

  public NGramPrefixGridStrategy(SpatialPrefixGrid grid, int maxLength) {
    super(grid, maxLength);
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    List<String> cells = simplifyGridCells(grid.readCells(shape));
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
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo field) {
    if (args.getOperation() != SpatialOperation.Intersects &&
        args.getOperation() != SpatialOperation.IsWithin &&
        args.getOperation() != SpatialOperation.Overlaps ){
      // TODO -- can translate these other query types
      throw new UnsupportedSpatialOperation(args.getOperation());
    }

    List<String> cells = simplifyGridCells(grid.readCells(args.getShape()));

    BooleanQuery booleanQuery = new BooleanQuery();
    for (String cell : cells) {
      booleanQuery.add(new TermQuery(new Term(field.getFieldName(), cell)), BooleanClause.Occur.SHOULD);
    }
    return booleanQuery;
  }

}
