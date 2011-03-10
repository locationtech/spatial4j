package org.apache.lucene.spatial.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;

public interface SpatialOperation<A extends SpatialArgs>
{
  public SpatialResult execute( AtomicReaderContext context, A args ) throws IOException;
}