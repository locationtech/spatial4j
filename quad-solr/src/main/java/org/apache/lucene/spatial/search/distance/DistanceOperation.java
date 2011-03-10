package org.apache.lucene.spatial.search.distance;

import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.spatial.search.SpatialOperation;
import org.apache.lucene.spatial.search.SpatialResult;

public class DistanceOperation implements SpatialOperation<DistanceArgs>
{    
  @Override
  public SpatialResult execute( AtomicReaderContext readerContext, DistanceArgs args ) throws IOException
  {
    SpatialResult res = new SpatialResult();
    //...
    return res;
  }
}
