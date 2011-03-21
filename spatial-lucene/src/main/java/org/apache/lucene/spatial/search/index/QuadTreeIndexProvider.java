package org.apache.lucene.spatial.search.index;

import org.apache.lucene.spatial.base.ShapeIO;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class QuadTreeIndexProvider extends CachedIndexProvider
{
  public QuadTreeIndexProvider( String shapeField, ShapeIO reader )
  {
    super( shapeField, reader );
  }

  @Override
  protected SpatialIndex createEmptyIndex()
  {
    return new Quadtree();
  }
}
