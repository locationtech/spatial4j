package org.apache.lucene.spatial.search.geo;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.util.DocIdBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeometryOperationFilter extends Filter
{
  static final Logger log = LoggerFactory.getLogger( GeometryOperationFilter.class );

  final Shape shape;

  public GeometryOperationFilter( Shape shape )
  {
    this.shape = shape;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context) throws IOException
  {
    final BitSet bits = new BitSet();

    // check the operation...

    return new DocIdBitSet( bits );
  }
}
