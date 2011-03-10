package org.apache.lucene.spatial.search;

import org.apache.lucene.util.Bits;

public class SpatialArgs
{
  public String field = null;
  public boolean calculateMatches = true;
  public boolean calculateValues = false;
  public Bits possibleItems = null; // easy way to skip many docs
}