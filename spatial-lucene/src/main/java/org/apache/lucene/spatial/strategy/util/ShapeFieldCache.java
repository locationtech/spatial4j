package org.apache.lucene.spatial.strategy.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.spatial.base.shape.Shape;

public class ShapeFieldCache<T extends Shape> {
  private List<T>[] cache;
  public int defaultLength;

  public ShapeFieldCache( int length, int defaultLength ) {
    cache = new List[length];
    this.defaultLength= defaultLength;
  }

  public void add( int docid, T s ) {
    List<T> list = cache[docid];
    if( list == null ) {
      list = cache[docid] = new ArrayList<T>(defaultLength);
    }
    list.add( s );
  }

  public List<T> getShapes( int docid ) {
    return cache[docid];
  }
}
