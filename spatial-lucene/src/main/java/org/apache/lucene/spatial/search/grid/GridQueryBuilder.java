package org.apache.lucene.spatial.search.grid;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

public class GridQueryBuilder extends SpatialQueryBuilder
{

  @Override
  public ValueSource makeValueSource(SpatialArgs args)
  {
    return null;
  }

  @Override
  public Query getQuery(SpatialArgs args)
  {
    // make a big boolean query...  set boost based on length?

    return new MatchAllDocsQuery();
  }
}
