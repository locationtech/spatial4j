package org.apache.lucene.spatial.search.index;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

import com.vividsolutions.jts.index.SpatialIndex;

public interface SpatialIndexProvider
{
  /**
   * This expects a SpatialIndex where the object is an 'int' pointing to the doc
   */
  public SpatialIndex getSpatialIndex( IndexReader reader ) throws CorruptIndexException, IOException;
}