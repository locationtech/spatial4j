package org.apache.lucene.spatial.search.index;

import org.apache.lucene.spatial.base.ShapeIO;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class STRTreeIndexProvider extends CachedIndexProvider
{
  protected int nodeCapacity;
  
  public STRTreeIndexProvider( int nodeCapacity, String shapeField, ShapeIO reader )
  {
    super( shapeField, reader );
    this.nodeCapacity = nodeCapacity;
  }
  
  @Override
  protected SpatialIndex createEmptyIndex()
  {
    return new STRtree( nodeCapacity );
  }
}
