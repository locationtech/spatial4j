package org.apache.lucene.spatial.strategy.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ShapeListFieldCache<T extends Shape> {
  static final Logger log = LoggerFactory.getLogger(ShapeListFieldCache.class);
  WeakHashMap<IndexReader, List<T>[]> sidx = new WeakHashMap<IndexReader, List<T>[]>();

  protected final int defaultSize;
  protected final String shapeField;

  public ShapeListFieldCache(String shapeField, int defaultSize) {
    this.shapeField = shapeField;
    this.defaultSize = defaultSize;
  }

  protected abstract T getShape( BytesRef term );

  public synchronized List<T>[] getSpatialIndex(IndexReader reader) throws CorruptIndexException, IOException {
    List<T>[] idx = sidx.get(reader);
    if (idx != null) {
      return idx;
    }
    long startTime = System.currentTimeMillis();
    
    log.info("Building Cache [" + reader.maxDoc() + "]");
    idx = new List[reader.maxDoc()];
    int count = 0;
    DocsEnum docs = null;
    Terms terms = reader.terms(shapeField);
    if (terms != null) {
      TermsEnum te = terms.iterator();
      BytesRef term = te.next();
      while (term != null) {
        T shape = getShape(term);
        docs = te.docs(null, docs);
        Integer docid = docs.nextDoc();
        while (docid != DocIdSetIterator.NO_MORE_DOCS) {
          List<T> list = (List<T>)idx[docid];
          if( list == null ) {
            list = new ArrayList<T>(defaultSize);
          }
          list.add( shape );
          docid = docs.nextDoc();
          count++;
        }
        term = te.next();
      }
    }
    long elapsed = System.currentTimeMillis() - startTime;
    log.info("Cached: [" + count + " in " + elapsed + "ms] " + idx);
    return idx;
  }
}
