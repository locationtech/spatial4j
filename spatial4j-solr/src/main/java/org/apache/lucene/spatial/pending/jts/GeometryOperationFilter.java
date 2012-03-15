/*
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
package org.apache.lucene.spatial.pending.jts;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.DocValues.Source;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DocIdBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;


public class GeometryOperationFilter extends Filter {

  static final Logger log = LoggerFactory.getLogger(GeometryOperationFilter.class);

  final String fieldName;
  final JtsSpatialContext shapeIO;
  final GeometryTest tester;

  public GeometryOperationFilter(String fieldName, GeometryTest tester, JtsSpatialContext shapeIO) {
    this.fieldName = fieldName;
    this.shapeIO = shapeIO;
    this.tester = tester;
  }

  @Override
  public DocIdSet getDocIdSet(final AtomicReaderContext context, final Bits acceptDocs) throws IOException {
    
//    return new DocIdSet() {
//      
//      @Override
//      public DocIdSetIterator iterator() throws IOException {
//        return new DocIdSetIterator() {
//          int id = 0;
//          
//          @Override
//          public int nextDoc() throws IOException {
//            if(++id > context.reader.maxDoc()) {
//              id = DocIdSetIterator.NO_MORE_DOCS;
//            }
//            return id;
//          }
//          
//          @Override
//          public int docID() {
//            return id;
//          }
//          
//          @Override
//          public int advance(int target) throws IOException {
//            if(target > context.reader.maxDoc()) {
//              return id = DocIdSetIterator.NO_MORE_DOCS;
//            }
//            return id = target;
//          }
//        };
//      }
//      
//
//      @Override
//      public boolean isCacheable() {
//        return false;
//      }
//      
//      @Override
//      public Bits bits() throws IOException {
//        return new Bits() {
//          DocValues vals = context.reader.docValues(fieldName);
//          Source src = vals.getDirectSource(); // read off disk (not in RAM)
//          WKBReader reader = new WKBReader(shapeIO.factory);
//          BytesRef bytes = new BytesRef(10000);
//          
//          @Override
//          public int length() {
//            return context.reader.maxDoc();
//          }
//          
//          @Override
//          public boolean get(int index) {
//            if( acceptDocs == null || acceptDocs.get(index)) {
//              bytes = src.getBytes(index, bytes);
//              if(bytes.length > 0) {
//                try {
//                  final BytesRef ref = bytes;
//                  Geometry geo = reader.read(new InStream() {
//                    int off = ref.offset;
//
//                    @Override
//                    public void read(byte[] buf) throws IOException {
//                      if ( buf.length > ref.length+off ) {
//                        throw new InvalidShapeException("Asking for too many bytes");
//                      }
//                      for (int i = 0; i < buf.length; i++) {
//                        buf[i] = ref.bytes[off + i];
//                      }
//                      off += buf.length;
//                    }
//                  });
//
//                  return tester.matches(geo);
//                }
//                catch (Exception ex) {
//                  log.warn("error testing geometry", ex);
//                }
//              }
//            }
//            return false;
//          }
//        };
//      }
//    };
    
    final BitSet bits = new BitSet();
    AtomicReader areader = context.reader();

    DocValues vals = areader.docValues(fieldName);
    Source src = vals.getDirectSource(); // read off disk (not in RAM)
    WKBReader reader = new WKBReader(shapeIO.factory);

    // TODO??? is this really the best way?  this checks *every* document -- even if some share the same value.  perhaps use sorted?
    BytesRef bytes = new BytesRef(10000);
    for( int docID=0; docID<areader.maxDoc(); docID++ ) {
      if( acceptDocs == null || acceptDocs.get(docID)) {
        bytes = src.getBytes(docID, bytes);
        if(bytes.length > 0) {
          try {
            final BytesRef ref = bytes;
            Geometry geo = reader.read(new InStream() {
              int off = ref.offset;

              @Override
              public void read(byte[] buf) throws IOException {
                if ( buf.length > ref.length+off ) {
                  throw new InvalidShapeException("Asking for too many bytes");
                }
                for (int i = 0; i < buf.length; i++) {
                  buf[i] = ref.bytes[off + i];
                }
                off += buf.length;
              }
            });

            if (tester.matches(geo)) {
              bits.set(docID);
            }
          }
          catch (ParseException ex) {
            log.warn("error reading indexed geometry", ex);
          }
        }
      }
    }

    return new DocIdBitSet(bits);
  }
}
