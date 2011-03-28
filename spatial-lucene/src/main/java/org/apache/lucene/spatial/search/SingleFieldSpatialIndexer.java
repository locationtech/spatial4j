package org.apache.lucene.spatial.search;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * Spatial indexer that only writes one field
 */
public abstract class SingleFieldSpatialIndexer<T extends SpatialFieldInfo> implements SpatialIndexer<T> {

  public boolean isPolyField() {
    return false;
  }

  public abstract Fieldable createField(T indexInfo, Shape shape, boolean index, boolean store);

  public Fieldable[] createFields(T indexInfo, Shape shape, boolean index, boolean store) {
    return new Fieldable[] { createField(indexInfo, shape, index, store) };
  }
}
