package org.apache.lucene.spatial.search;

import org.apache.lucene.util.Bits;

public class SpatialResult
{
  public Bits matches;     // which docs match... maybe DocIdSet?
  public double[] values;  // any values we have calculated, maybe Map<Integer,Object>? or just Object[]
  boolean cacheable = false;
}
