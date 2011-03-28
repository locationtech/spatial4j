package org.apache.lucene.spatial.search.point;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.search.SpatialIndexer;

public class PointSpatialIndexer implements SpatialIndexer<PointFieldInfo> {

  @Override
  public Fieldable[] createFields(PointFieldInfo indexInfo,
      Shape shape, boolean index, boolean store) {
    throw new UnsupportedOperationException("not implemented yet (in solr for now)");
  }
}
