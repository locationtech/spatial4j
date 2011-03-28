package org.apache.lucene.spatial.search;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * must be thread safe
 */
public abstract class SpatialIndexer<T extends SpatialFieldInfo> {

  protected boolean ignoreIncompatibleGeometry = false;

  public boolean isPolyField() {
    return false;
  }

  public abstract Fieldable createField(T fieldInfo, Shape shape, boolean index, boolean store);

  public Fieldable[] createFields(T fieldInfo, Shape shape, boolean index, boolean store) {
    return new Fieldable[] { createField(fieldInfo, shape, index, store) };
  }

  public boolean isIgnoreIncompatibleGeometry() {
    return ignoreIncompatibleGeometry;
  }

  public void setIgnoreIncompatibleGeometry(boolean ignoreIncompatibleGeometry) {
    this.ignoreIncompatibleGeometry = ignoreIncompatibleGeometry;
  }
}
