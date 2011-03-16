package org.apache.lucene.spatial.search.index;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.DocIdBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;


public class SpatialIndexFilter extends Filter
{
  static final Logger log = LoggerFactory.getLogger( SpatialIndexFilter.class );

  final SpatialIndexProvider provider;
  final Envelope bounds;

  public SpatialIndexFilter( SpatialIndexProvider sidx, Envelope bounds )
  {
    this.provider = sidx;
    this.bounds = bounds;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context) throws IOException 
  {
    SpatialIndex sidx = provider.getSpatialIndex( context.reader );
    final BitSet bits = new BitSet();    
    sidx.query( bounds, new ItemVisitor() {
      @Override
      public void visitItem(Object item) {
        bits.set( (Integer)item );
      }
    });
    return new DocIdBitSet( bits );
  }
}
