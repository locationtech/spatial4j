package org.apache.lucene.spatial.search;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.SpatialArgs;

/**
 * must be thread safe
 */
public interface SpatialQueryBuilder<T>
{
  public Fieldable[] createFields( T field, Shape shape, boolean index, boolean store );
  public ValueSource makeValueSource(SpatialArgs args, T field);
  public Query makeQuery(SpatialArgs args, T field);
}
