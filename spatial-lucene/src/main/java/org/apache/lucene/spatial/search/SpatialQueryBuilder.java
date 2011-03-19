package org.apache.lucene.spatial.search;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.SpatialOperation;

public abstract class SpatialQueryBuilder
{
  public abstract ValueSource makeValueSource( SpatialOperation op );
  public abstract Query getQuery( SpatialOperation op );
}
