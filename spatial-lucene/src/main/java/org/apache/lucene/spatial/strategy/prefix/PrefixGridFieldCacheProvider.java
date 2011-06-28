package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class PrefixGridFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialPrefixGrid grid; //

  public PrefixGridFieldCacheProvider(SpatialPrefixGrid grid, String shapeField, int defaultSize) {
    super( shapeField, defaultSize );
    this.grid = grid;
  }

  //A kluge that this is a field
  private SpatialPrefixGrid.Cell scanCell = null;

  @Override
  protected Point readShape(BytesRef term) {
    scanCell = grid.getCell(term.bytes, term.offset, term.length, scanCell);
    return scanCell.isLeaf() ? scanCell.getShape().getCenter() : null;
  }
}
