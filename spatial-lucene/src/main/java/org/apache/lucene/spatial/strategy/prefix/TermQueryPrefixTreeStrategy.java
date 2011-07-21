package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.prefix.Node;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixTree;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;

import java.util.List;

public class TermQueryPrefixTreeStrategy extends PrefixTreeStrategy {

  public TermQueryPrefixTreeStrategy(SpatialPrefixTree grid) {
    super(grid);
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
    Shape qshape = args.getShape();
    int detailLevel = grid.getMaxLevelForPrecision(qshape, args.getDistPrecision());
    List<Node> cells = grid.getNodes(qshape, detailLevel, false);

    BooleanQuery booleanQuery = new BooleanQuery();
    for (Node cell : cells) {
      booleanQuery.add(new TermQuery(new Term(field.getFieldName(), cell.getTokenString())), BooleanClause.Occur.SHOULD);
    }
    return booleanQuery;
  }

}
