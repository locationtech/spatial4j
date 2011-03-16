package org.apache.lucene.spatial.search.index;

import org.apache.lucene.spatial.core.ShapeReader;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class STRTreeIndexProvider extends CachedIndexProvider
{
  protected int nodeCapacity;
  
  public STRTreeIndexProvider( int nodeCapacity, String shapeField, ShapeReader reader )
  {
    super( shapeField, reader );
    this.nodeCapacity = nodeCapacity;
  }
  
  protected SpatialIndex createEmptyIndex()
  {
    return new STRtree( nodeCapacity );
  }
}
