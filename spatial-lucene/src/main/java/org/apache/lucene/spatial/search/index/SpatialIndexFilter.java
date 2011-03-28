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
import java.util.BitSet;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.lucene.spatial.base.shape.jts.JtsEnvelope;
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
  final SpatialOperation op;

  public SpatialIndexFilter( SpatialIndexProvider sidx, SpatialArgs args )
  {
    this.provider = sidx;
    this.op = args.getOperation();

    BBox bbox = args.getShape().getBoundingBox();
    if (bbox instanceof JtsEnvelope) {
      this.bounds = ((JtsEnvelope)bbox).envelope;
    }
    else {
      if (op != SpatialOperation.BBoxIntersects) {
        throw new UnsupportedOperationException(op.name() + " for shape: " + args.getShape());
      }
      this.bounds = new Envelope(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY() );
    }

    if( !(op == SpatialOperation.Intersects || op == SpatialOperation.BBoxIntersects) ) {
      throw new UnsupportedOperationException( op.name() );
    }
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
