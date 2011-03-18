package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.spatial.base.BBox;

public interface BBoxSimilarity {
  public double score( BBox extent );
}
