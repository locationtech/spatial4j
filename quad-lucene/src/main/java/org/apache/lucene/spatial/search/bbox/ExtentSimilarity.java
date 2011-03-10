package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.spatial.core.Extent;

public interface ExtentSimilarity {
  public float score( Extent extent );
}
