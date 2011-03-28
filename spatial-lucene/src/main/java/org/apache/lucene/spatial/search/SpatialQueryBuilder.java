package org.apache.lucene.spatial.search;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.query.SpatialArgs;

/**
 * must be thread safe
 */
public interface SpatialQueryBuilder<T extends SpatialFieldInfo> {

  public ValueSource makeValueSource(SpatialArgs args, T field);
  public Query makeQuery(SpatialArgs args, T field);
}
