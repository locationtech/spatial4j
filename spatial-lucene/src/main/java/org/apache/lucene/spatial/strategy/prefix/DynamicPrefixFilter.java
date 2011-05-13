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

package org.apache.lucene.spatial.strategy.prefix;

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
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;

/**
 * Performs a dynamic length spatial filter against a field indexed using an NGram based {@link SpatialPrefixGrid}.
 * This filter recursively traverses each grid length and uses methods on {@link Shape} to efficiently know
 * that all points at a prefix fit in the shape or not to either short-circuit unnecessary traversals or to efficiently
 * load all enclosed points.
 *
 */
public class DynamicPrefixFilter extends Filter {

  private final String fieldName;
  private final SpatialPrefixGrid grid;
  private final Shape queryShape;
  private final int prefixGridScanLevel;//at least one less than grid.getMaxLevels()

  public DynamicPrefixFilter(String fieldName, SpatialPrefixGrid grid, Shape queryShape, int prefixGridScanLevel) {
    this.fieldName = fieldName;
    this.grid = grid;
    this.queryShape = queryShape;
    this.prefixGridScanLevel = Math.min(prefixGridScanLevel,grid.getMaxLevels()-1);
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

    //cells is treated like a stack. LinkedList conveniently has bulk add to beginning.
    LinkedList<SpatialPrefixGrid.Cell> cells = new LinkedList<SpatialPrefixGrid.Cell>(grid.getCells(queryShape));

    while(!cells.isEmpty() && term != null) {
      final SpatialPrefixGrid.Cell cell = cells.removeFirst();
      assert cell.getLevel() > 0;
      final BytesRef cellTerm = new BytesRef(cell.getBytes());
      //if term is "ahead" of this cell (not within and comes after) then short-circuit; continue onto the next cell
      if (!term.startsWith(cellTerm) && cellTerm.compareTo(term) < 0)
        continue; //TODO DWS: !! double-check logic correction
      IntersectCase intersection = queryShape.intersect(cell.getShape(), grid.getShapeIO());
      if (intersection == IntersectCase.OUTSIDE)
        continue;
      TermsEnum.SeekStatus seekStat = termsEnum.seek(cellTerm);
      if (seekStat == TermsEnum.SeekStatus.END)
        break;
      term = termsEnum.term();
      if (seekStat == TermsEnum.SeekStatus.NOT_FOUND)
        continue;
      if (intersection == IntersectCase.CONTAINS) {
        docsEnum = termsEnum.docs(delDocs, docsEnum);
        addDocs(docsEnum,bits);
        term = termsEnum.next();//move to next term
      } else {//any other intersection
        //TODO is it worth it to optimize the shape (e.g. potentially simpler polygon)?

        //We either scan through the leaf cell(s), or if there are many points then we divide & conquer.
        boolean manyPoints = cell.getLevel() < prefixGridScanLevel;

        //TODO Try variable depth strategy:
        //IF configured to do so, we could use term.freq() as an estimate on the number of places at this depth.  OR, perhaps
        //  make estimates based on the total known term count at this level?  Or don't worry about it--use fixed depth.
//        if (manyPoints) {
//          //Make some estimations on how many points there are at this level and how few there would need to be to set
//          // manyPoints to false.
//
//          long termsThreshold = (long) estimateNumberIndexedTerms(cell.length(),queryShape.getDocFreqExpenseThreshold(cell));
//
//          long thisOrd = termsEnum.ord();
//          manyPoints = (termsEnum.seek(thisOrd+termsThreshold+1) != TermsEnum.SeekStatus.END
//                  && cell.contains(termsEnum.term()));
//          termsEnum.seek(thisOrd);//return to last position
//        }

        if (!manyPoints) {
          //traverse all leaf terms within this cell to see if they are within the queryShape, one by one.
          for(; term != null && term.startsWith(cellTerm); term = termsEnum.next()) {
            Point p = grid.getPoint(term.utf8ToString());
            if (p == null)
              continue;
            IntersectCase relation = queryShape.intersect(p, grid.getShapeIO());
            //TODO !! refactor intersect implementations
            if(relation == IntersectCase.OUTSIDE)
              continue;

            docsEnum = termsEnum.docs(delDocs, docsEnum);
            addDocs(docsEnum,bits);
          }
        } else {
          //divide & conquer
          cells.addAll(0, cell.getSubCells());//add to beginning
        }
      }
    }//cell loop

    return bits;
  }

//  double estimateNumberIndexedTerms(int cellLen,double points) {
//    return 1000;
//    double levelProb = probabilityNumCells[points];// [1,32)
//    if (cellLen < geohashLength)
//      return levelProb + levelProb * estimateNumberIndexedTerms(cellLen+1,points/levelProb);
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

    DynamicPrefixFilter that = (DynamicPrefixFilter) o;

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
