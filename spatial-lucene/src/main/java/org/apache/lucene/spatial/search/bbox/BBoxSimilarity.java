package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.spatial.core.BBox;

public interface BBoxSimilarity {
  public double score( BBox extent );
}
