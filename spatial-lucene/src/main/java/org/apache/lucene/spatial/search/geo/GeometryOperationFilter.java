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
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.ShapeIO;
import org.apache.lucene.spatial.base.ShapeTester;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DocIdBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeometryOperationFilter extends Filter
{
  static final Logger log = LoggerFactory.getLogger( GeometryOperationFilter.class );

  final String fname;
  final SpatialArgs args;
  final ShapeIO reader;
  final ShapeTester tester;

  public GeometryOperationFilter( String fname, SpatialArgs args, ShapeIO reader )
  {
    this.fname = fname;
    this.args = args;
    this.reader = reader;

    // TODO, make a test based on teh shape!!!
    tester = new ShapeTester() {
      @Override
      public boolean matches(Shape shape) {
        return true;
      }
    };
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
        Shape shape = reader.readShape( term.bytes, term.offset, term.length );
        if( tester.matches( shape ) ) {
          // now add everything that matches
          docs = te.docs(null, docs);
          int docid = docs.nextDoc();
          while( docid != DocIdSetIterator.NO_MORE_DOCS ) {
            bits.set( docid );
            docid = docs.nextDoc();
          }
        }
        term = te.next();
      }
    }
    return new DocIdBitSet( bits );
  }
}
