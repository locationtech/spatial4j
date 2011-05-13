package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class PrefixFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialPrefixGrid grid; //

  public PrefixFieldCacheProvider(SpatialPrefixGrid grid, String shapeField, int defaultSize) {
    super( shapeField, defaultSize );
    this.grid = grid;
  }

  @Override
  protected Point readShape(BytesRef term) {
    final String token = term.utf8ToString();
    return grid.getPoint(token);
  }
}
