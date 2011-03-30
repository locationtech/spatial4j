package org.apache.lucene.spatial.strategy;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * must be thread safe
 */
public abstract class SpatialStrategy<T extends SpatialFieldInfo> {

  protected boolean ignoreIncompatibleGeometry = false;

  public boolean isPolyField() {
    return false;
  }

  public abstract Fieldable createField(T fieldInfo, Shape shape, boolean index, boolean store);

  public Fieldable[] createFields(T fieldInfo, Shape shape, boolean index, boolean store) {
    return new Fieldable[] { createField(fieldInfo, shape, index, store) };
  }

  public abstract ValueSource makeValueSource(SpatialArgs args, T field);
  public abstract Query makeQuery(SpatialArgs args, T field);

  public boolean isIgnoreIncompatibleGeometry() {
    return ignoreIncompatibleGeometry;
  }

  public void setIgnoreIncompatibleGeometry(boolean ignoreIncompatibleGeometry) {
    this.ignoreIncompatibleGeometry = ignoreIncompatibleGeometry;
  }
}
