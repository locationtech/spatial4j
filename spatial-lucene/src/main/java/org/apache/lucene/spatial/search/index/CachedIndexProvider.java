package org.apache.lucene.spatial.search.index;

import java.io.IOException;
import java.util.WeakHashMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.ShapeIO;
import org.apache.lucene.spatial.base.jts.JtsEnvelope;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;


/**
 * This uses a WeakHashMap to hold an in-memory index
 */
public abstract class CachedIndexProvider implements SpatialIndexProvider
{
  static final Logger log = LoggerFactory.getLogger( CachedIndexProvider.class );
  WeakHashMap<IndexReader, SpatialIndex> sidx = new WeakHashMap<IndexReader, SpatialIndex>();

  protected final String shapeField;
  protected final ShapeIO shapeReader;
  
  public CachedIndexProvider( String shapeField, ShapeIO reader )
  {
    this.shapeField = shapeField;
    this.shapeReader = reader;
  }
  
  protected abstract SpatialIndex createEmptyIndex();
  
  @Override
  public SpatialIndex getSpatialIndex(IndexReader reader) throws CorruptIndexException, IOException 
  { 
    SpatialIndex idx = sidx.get( reader );
    if( idx == null ) {  // TODO, locking etc, make sure there is not overlap
      Long lastmodified = IndexReader.lastModified( reader.directory() );
      log.info( "Building RTree. "+lastmodified );
      idx = createEmptyIndex();

      DocsEnum docs = null;
      Terms terms = reader.terms(shapeField);
      if( terms != null ) {
        TermsEnum te = terms.iterator();
        BytesRef term = te.next();
        while( term != null ) {
          String txt = term.utf8ToString();
          Shape shape = shapeReader.readShape( txt );
          BBox bbox = shape.getBoundingBox();
          Envelope envelope = null;
          if( bbox instanceof JtsEnvelope ) {
            envelope = ((JtsEnvelope)bbox).envelope;
          }
          else {
            envelope = new Envelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY() );
          }

          docs = te.docs(null, docs);
          log.trace( "Add: "+txt + " ["+te.docFreq()+"] " );
          Integer docid = new Integer( docs.nextDoc() );
          while( docid != DocIdSetIterator.NO_MORE_DOCS ) {
            idx.insert( envelope, docid );
            log.trace( " "+docid );
            docid = new Integer( docs.nextDoc() );
          }
          term = te.next();
        }
      }
      idx.query( new Envelope( -1, 1, -1, 1 ) ); // this will build the index
      log.info( "initalized: "+idx );
      sidx.put( reader, idx );
    }
    return idx;
  }
}
