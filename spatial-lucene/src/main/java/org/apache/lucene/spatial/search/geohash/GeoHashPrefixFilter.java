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

package org.apache.lucene.spatial.search.geohash;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;

/**
 * Performs a spatial filter against a field indexed using Geohashes. Using the hierarchical grid nature of geohashes,
 * this filter recursively traverses each precision length and uses methods on {@link Geometry2D} to efficiently know
 * that all points at a geohash fit in the shape or not to either short-circuit unnecessary traversals or to efficiently
 * load all enclosed points.
 */
public class GeoHashPrefixFilter extends Filter {

  private static final int GRIDLEN_SCAN_THRESHOLD = 4;//>= 1

  private final String fieldName;
  private final GridReferenceSystem gridReferenceSystem;
  private final Shape queryShape;

  public GeoHashPrefixFilter(String fieldName, GridReferenceSystem gridReferenceSystem, Shape geoShape) {
    this.fieldName = fieldName;
    this.gridReferenceSystem = gridReferenceSystem;
    this.queryShape = geoShape;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext ctx) throws IOException {
    IndexReader reader = ctx.reader;
    OpenBitSet bits = new OpenBitSet(reader.maxDoc());
    Terms terms = reader.fields().terms(fieldName);
    if (terms == null)
      return null;
    TermsEnum termsEnum = terms.iterator();
    DocsEnum docsEnum = null;//cached for termsEnum.docs() calls
    Bits delDocs = reader.getDeletedDocs();
    BytesRef term = termsEnum.next();//the most recent term examined via termsEnum.term()

    //TODO Add a precision short-circuit so that we are not accurate on the edge but we're faster.

    //TODO An array based nodes impl would be more efficient; or a stack of iterators.  LinkedList conveniently has bulk add to beginning.
    LinkedList<GridNode> nodes = new LinkedList<GridNode>(gridReferenceSystem.getSubNodes(queryShape.getBoundingBox()));

    while(!nodes.isEmpty() && term != null) {
      final GridNode node = nodes.removeFirst();
      assert node.length() > 0;
      if (!node.contains(term) && node.before(term))
        continue;//short circuit, moving >= the next indexed term
      IntersectCase intersection = queryShape.intersect(node.getRectangle(),gridReferenceSystem.shapeIO);
      if (intersection == IntersectCase.OUTSIDE)
        continue;
      TermsEnum.SeekStatus seekStat = termsEnum.seek(node.getBytesRef());
      term = termsEnum.term();
      if (seekStat != TermsEnum.SeekStatus.FOUND)
        continue;
      if (intersection == IntersectCase.CONTAINS) {
        docsEnum = termsEnum.docs(delDocs, docsEnum);
        addDocs(docsEnum,bits);
        term = termsEnum.next();//move to next term
      } else {//any other intersection
        //TODO is it worth it to optimize the shape (e.g. potentially simpler polygon)?
        //GeoShape geoShape = this.geoShape.optimize(intersection);

        //We either scan through the leaf node(s), or if there are many points then we divide & conquer.
        boolean manyPoints = node.length() < gridReferenceSystem.maxLen - GRIDLEN_SCAN_THRESHOLD;

        //TODO Try variable depth strategy:
        //IF configured to do so, we could use term.freq() as an estimate on the number of places at this depth.  OR, perhaps
        //  make estimates based on the total known term count at this level?  Or don't worry about it--use fixed depth.
//        if (manyPoints) {
//          //Make some estimations on how many points there are at this level and how few there would need to be to set
//          // manyPoints to false.
//
//          long termsThreshold = (long) estimateNumberIndexedTerms(node.length(),geoShape.getDocFreqExpenseThreshold(node));
//
//          long thisOrd = termsEnum.ord();
//          manyPoints = (termsEnum.seek(thisOrd+termsThreshold+1) != TermsEnum.SeekStatus.END
//                  && node.contains(termsEnum.term()));
//          termsEnum.seek(thisOrd);//return to last position
//        }

        if (!manyPoints) {
          //traverse all leaf terms within this node to see if they are within the geoShape, one by one.
          for(; term != null && node.contains(term); term = termsEnum.next()) {
            if (term.length < gridReferenceSystem.maxLen)//not a leaf
              continue;

            Point p = gridReferenceSystem.decodeXY(term);
            IntersectCase relation = p.intersect(queryShape,gridReferenceSystem.shapeIO);
            if(relation != IntersectCase.CONTAINS)
              continue;

            docsEnum = termsEnum.docs(delDocs, docsEnum);
            addDocs(docsEnum,bits);
          }
        } else {
          //divide & conquer
          nodes.addAll(0,node.getSubNodes());//add to beginning
        }
      }
    }//node loop

    return bits;
  }

//  double estimateNumberIndexedTerms(int nodeLen,double points) {
//    return 1000;
//    double levelProb = probabilityNumNodes[points];// [1,32)
//    if (nodeLen < geohashLength)
//      return levelProb + levelProb * estimateNumberIndexedTerms(nodeLen+1,points/levelProb);
//    return levelProb;
//  }

  private void addDocs(DocsEnum docsEnum, OpenBitSet bits) throws IOException {
    DocsEnum.BulkReadResult bulk = docsEnum.getBulkResult();
    for (; ;) {
      int nDocs = docsEnum.read();
      if (nDocs == 0) break;
      int[] docArr = bulk.docs.ints;
      int end = bulk.docs.offset + nDocs;
      for (int i = bulk.docs.offset; i < end; i++) {
        bits.fastSet(docArr[i]);
      }
    }
  }

  @Override
  public String toString() {
    return "GeoFilter{fieldName='" + fieldName + '\'' + ", shape=" + queryShape + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GeoHashPrefixFilter that = (GeoHashPrefixFilter) o;

    if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
    if (queryShape != null ? !queryShape.equals(that.queryShape) : that.queryShape != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fieldName != null ? fieldName.hashCode() : 0;
    result = 31 * result + (queryShape != null ? queryShape.hashCode() : 0);
    return result;
  }
}
