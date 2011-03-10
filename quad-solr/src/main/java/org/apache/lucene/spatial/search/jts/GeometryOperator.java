package org.apache.lucene.spatial.search.jts;

import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.spatial.search.SpatialOperation;
import org.apache.lucene.spatial.search.SpatialResult;

public class GeometryOperator implements SpatialOperation<GeometrySpatialArguments>
{
  @Override
  public SpatialResult execute( AtomicReaderContext readerContext, GeometrySpatialArguments args ) throws IOException
  {
    SpatialResult res = new SpatialResult();
    //...
    return res;
  }
}