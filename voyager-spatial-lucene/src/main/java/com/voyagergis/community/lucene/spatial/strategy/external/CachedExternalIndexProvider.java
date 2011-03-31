/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.voyagergis.community.lucene.spatial.strategy.external;

import java.io.IOException;
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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.voyagergis.community.lucene.spatial.shape.JtsEnvelope;


/**
 * This uses a WeakHashMap to hold an in-memory index
 */
public abstract class CachedExternalIndexProvider implements ExternalSpatialIndexProvider {
  static final Logger log = LoggerFactory.getLogger(CachedExternalIndexProvider.class);
  WeakHashMap<IndexReader, SpatialIndex> sidx = new WeakHashMap<IndexReader, SpatialIndex>();

  protected final String shapeField;
  protected final SpatialContext shapeReader;

  public CachedExternalIndexProvider(String shapeField, SpatialContext reader) {
    this.shapeField = shapeField;
    this.shapeReader = reader;
  }

  protected abstract SpatialIndex createEmptyIndex();

  @Override
  public synchronized SpatialIndex getSpatialIndex(IndexReader reader) throws CorruptIndexException, IOException {
    SpatialIndex idx = sidx.get(reader);
    if (idx != null) {
      return idx;
    }
    long startTime = System.currentTimeMillis();
    Long lastmodified = IndexReader.lastModified(reader.directory());

    if (log.isInfoEnabled()) {
      log.info("Building Index. " + lastmodified + " [" + reader.maxDoc() + "]");
    }
    idx = createEmptyIndex();

    int count = 0;
    DocsEnum docs = null;
    Terms terms = reader.terms(shapeField);
    if (terms != null) {
      TermsEnum te = terms.iterator();
      BytesRef term = te.next();
      while (term != null) {
        String txt = term.utf8ToString();
        Shape shape = shapeReader.readShape(txt);
        BBox bbox = shape.getBoundingBox();
        Envelope envelope;
        if (JtsEnvelope.class.isInstance(bbox)) {
          envelope = ((JtsEnvelope) bbox).envelope;
        } else {
          envelope = new Envelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY());
        }

        docs = te.docs(null, docs);

        if (log.isTraceEnabled()) {
          log.trace("Add: " + txt + " [" + te.docFreq() + "] ");
        }

        Integer docid = docs.nextDoc();
        while (docid != DocIdSetIterator.NO_MORE_DOCS) {
          idx.insert(envelope, docid);

          if (log.isTraceEnabled()) {
            log.trace(" " + docid);
          }

          docid = docs.nextDoc();
          count++;
        }
        term = te.next();
      }
    }
    long elapsed = System.currentTimeMillis() - startTime;
    idx.query(new Envelope(-1, 1, -1, 1)); // this will build the index
    sidx.put(reader, idx);

    if (log.isInfoEnabled()) {
      log.info("Indexed: [" + count + " in " + elapsed + "ms] " + idx);
    }
    return idx;
  }
}
