package org.apache.lucene.spatial.search;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.core.SpatialOperation;

public abstract class SpatialQueryBuilder
{
  public abstract ValueSource makeValueSource( SpatialOperation op );
  public abstract Query getFilterQuery( SpatialOperation op );
}
