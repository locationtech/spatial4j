package org.apache.lucene.spatial.core.bbox;

import org.apache.lucene.spatial.core.Extent;

public interface ExtentSimilarity {
  public double score( Extent extent );
}
