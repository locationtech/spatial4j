package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class GeoHashFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialContext context;

  public GeoHashFieldCacheProvider( SpatialContext ctx, String shapeField, int defaultSize ) {
    super( shapeField, defaultSize );
    this.context = ctx;
  }

  @Override
  protected Point readShape(BytesRef term) {
    // TODO Actually fill in the point...
    return context.makePoint( 0, 0 );
  }
}
