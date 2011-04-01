package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class GeoHashFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialContext context; //

  public GeoHashFieldCacheProvider( SpatialContext ctx, String shapeField, int defaultSize ) {
    super( shapeField, defaultSize );
    this.context = ctx;
  }

  @Override
  protected Point readShape(BytesRef term) {
    // TODO skip ngrams?
    double[] p = GeoHashUtils.decode(term.utf8ToString(), context);
    return context.makePoint( p[0], p[1] );
  }
}
