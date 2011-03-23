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
  public synchronized SpatialIndex getSpatialIndex(IndexReader reader) throws CorruptIndexException, IOException
  {
    SpatialIndex idx = sidx.get( reader );
    if( idx == null ) {
      long startTime = System.currentTimeMillis();
      Long lastmodified = IndexReader.lastModified( reader.directory() );
      log.info( "Building Index. "+lastmodified + " ["+reader.maxDoc()+"]" );
      idx = createEmptyIndex();

      int count = 0;
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
            count++;
          }
          term = te.next();
        }
      }
      long elapsed = System.currentTimeMillis() - startTime;
      idx.query( new Envelope( -1, 1, -1, 1 ) ); // this will build the index
      log.info( "Indexed: ["+count+" in "+elapsed+"ms] "+idx );
      sidx.put( reader, idx );
    }
    return idx;
  }
}
