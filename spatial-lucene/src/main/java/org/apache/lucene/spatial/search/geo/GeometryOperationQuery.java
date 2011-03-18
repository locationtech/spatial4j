package org.apache.lucene.spatial.search.geo;

import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeometryOperationQuery extends Query
{
  static final Logger log = LoggerFactory.getLogger( GeometryOperationQuery.class );

  final Shape shape;

  public GeometryOperationQuery( Shape shape )
  {
    this.shape = shape;
  }

  @Override
  public String toString(String field) {
    // TODO Auto-generated method stub
    return null;
  }

}
