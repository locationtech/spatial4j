package org.apache.lucene.spatial.search.grid;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.core.SpatialOperation;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

public class GridQueryBuilder extends SpatialQueryBuilder
{

  @Override
  public ValueSource makeValueSource(SpatialOperation op)
  {
    return null;
  }

  @Override
  public Query getFilterQuery(SpatialOperation op)
  { 
    // make a big boolean query...  set boost based on length?

    
    
    return null;
  }
}
