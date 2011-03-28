package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.search.SpatialIndexer;

public class BBoxSpatialIndexer implements SpatialIndexer<BBoxFieldInfo> {

  @Override
  public Fieldable[] createFields(BBoxFieldInfo indexInfo,
      Shape shape, boolean index, boolean store) {
    throw new UnsupportedOperationException("not implemented yet (in solr for now)");
  }
}
