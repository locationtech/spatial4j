package org.apache.lucene.spatial.search.extent;

import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.core.Extent;

public interface ExtentQueryFactory
{
  public Query makeQuery( Extent input, ExtentFieldNameInfo fields, boolean ranking );
}
