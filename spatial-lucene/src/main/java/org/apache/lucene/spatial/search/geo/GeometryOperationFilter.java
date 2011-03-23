package org.apache.lucene.spatial.search.geo;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DocIdBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;


public class GeometryOperationFilter extends Filter
{
  static final Logger log = LoggerFactory.getLogger( GeometryOperationFilter.class );

  final String fname;
  final SpatialArgs args;
  final GeometryFactory factory;
  final GeometryTester tester;

  public GeometryOperationFilter( String fname, SpatialArgs args, JTSShapeIO reader )
  {
    this.fname = fname;
    this.args = args;
    this.factory = reader.factory;
    tester = GeometryTestFactory.get( args, reader );
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context) throws IOException
  {
    final BitSet bits = new BitSet();

    DocsEnum docs = null;
    Terms terms = context.reader.terms(fname);
    if( terms != null ) {
      TermsEnum te = terms.iterator();
      BytesRef term = te.next();
      while( term != null ) {
        WKBReader reader = new WKBReader( factory );
        try {
          final BytesRef ref = term;
          Geometry geo = reader.read( new InStream(){
            int off = ref.offset;

            @Override
            public void read(byte[] buf) throws IOException {
              if( off+buf.length > ref.length ) {
                throw new InvalidShapeException( "Asking for too many bytes" );
              }
              for( int i=0;i<buf.length; i++ ) {
                buf[i] = ref.bytes[off+i];
              }
              off += buf.length;
            }
          });

          if( tester.matches( geo ) ) {
            // now add everything that matches
            docs = te.docs(null, docs);
            int docid = docs.nextDoc();
            while( docid != DocIdSetIterator.NO_MORE_DOCS ) {
              bits.set( docid );
              docid = docs.nextDoc();
            }
          }
        }
        catch( ParseException ex ) {
          log.warn( "error reading indexed geometry", ex );
        }
        term = te.next();
      }
    }
    return new DocIdBitSet( bits );
  }
}
