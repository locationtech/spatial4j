package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.Node;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixTree;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class PointPrefixTreeFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialPrefixTree grid; //

  public PointPrefixTreeFieldCacheProvider(SpatialPrefixTree grid, String shapeField, int defaultSize) {
    super( shapeField, defaultSize );
    this.grid = grid;
  }

  //A kluge that this is a field
  private Node scanCell = null;

  @Override
  protected Point readShape(BytesRef term) {
    scanCell = grid.getNode(term.bytes, term.offset, term.length, scanCell);
    return scanCell.isLeaf() ? scanCell.getShape().getCenter() : null;
  }
}
