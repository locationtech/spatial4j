package org.apache.lucene.spatial.strategy;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * must be thread safe
 */
public abstract class SpatialStrategy<T extends SpatialFieldInfo> {

  protected boolean ignoreIncompatibleGeometry = false;
  protected final SpatialContext ctx;

  public SpatialStrategy(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public SpatialContext getSpatialContext() {
    return ctx;
  }

  public boolean isPolyField() {
    return false;
  }

  /**
   * This may return a null field if it does not want to make anything.
   * This is reasonable behavior if 'ignoreIncompatibleGeometry=true' and the
   * geometry is incompatible
   */
  public abstract Fieldable createField(T fieldInfo, Shape shape, boolean index, boolean store);

  public Fieldable[] createFields(T fieldInfo, Shape shape, boolean index, boolean store) {
    return new Fieldable[] { createField(fieldInfo, shape, index, store) };
  }

  public abstract ValueSource makeValueSource(SpatialArgs args, T field);

  /**
   * Make a query
   */
  public abstract Query makeQuery(SpatialArgs args, T field);

  /**
   * Make a Filter
   */
  public abstract Filter makeFilter(SpatialArgs args, T field);

  public boolean isIgnoreIncompatibleGeometry() {
    return ignoreIncompatibleGeometry;
  }

  public void setIgnoreIncompatibleGeometry(boolean ignoreIncompatibleGeometry) {
    this.ignoreIncompatibleGeometry = ignoreIncompatibleGeometry;
  }
}
