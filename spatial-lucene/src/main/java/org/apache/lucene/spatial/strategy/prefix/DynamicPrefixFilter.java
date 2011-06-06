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

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Performs a dynamic length spatial filter against a field indexed using an NGram based {@link SpatialPrefixGrid}.
 * This filter recursively traverses each grid length and uses methods on {@link Shape} to efficiently know
 * that all points at a prefix fit in the shape or not to either short-circuit unnecessary traversals or to efficiently
 * load all enclosed points.
 */
public class DynamicPrefixFilter extends Filter {

  /* TODOs for future:

Add a precision short-circuit so that we are not accurate on the edge but we're faster.

Can a polygon query shape be optimized / made-simpler at recursive depths (e.g. intersection of shape + cell box)

RE "scan" threshold:
  // IF configured to do so, we could use term.freq() as an estimate on the number of places at this depth.  OR, perhaps
  //  make estimates based on the total known term count at this level?
  if (!scan) {
    //Make some estimations on how many points there are at this level and how few there would need to be to set
    // !scan to false.
    long termsThreshold = (long) estimateNumberIndexedTerms(cell.length(),queryShape.getDocFreqExpenseThreshold(cell));
    long thisOrd = termsEnum.ord();
    scan = (termsEnum.seek(thisOrd+termsThreshold+1) == TermsEnum.SeekStatus.END
            || !cell.contains(termsEnum.term()));
    termsEnum.seek(thisOrd);//return to last position
  }

  */

  private final String fieldName;
  private final SpatialPrefixGrid grid;
  private final Shape queryShape;
  private final int prefixGridScanLevel;//at least one less than grid.getMaxLevels()
  private final int detailLevel;//TODO not yet supported

  public DynamicPrefixFilter(String fieldName, SpatialPrefixGrid grid, Shape queryShape, int prefixGridScanLevel,
                             int detailLevel) {
    this.fieldName = fieldName;
    this.grid = grid;
    this.queryShape = queryShape;
    this.prefixGridScanLevel = Math.max(1,Math.min(prefixGridScanLevel,grid.getMaxLevels()-1));
    this.detailLevel = detailLevel;
    assert detailLevel <= grid.getMaxLevels();
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

    //cells is treated like a stack. LinkedList conveniently has bulk add to beginning. It's in sorted order so that we
    //  always advance forward through the termsEnum index.
    LinkedList<SpatialPrefixGrid.Cell> cells = new LinkedList<SpatialPrefixGrid.Cell>(grid.getWorldCell().getSubCells());

    //This is a recursive algorithm that starts with one or more "big" cells, and then recursively dives down into the
    // first such cell that intersects with the query shape.  It's a depth first traversal because we don't move onto
    // the next big cell (breadth) until we're completely done considering all smaller cells beneath it. For a given
    // cell, if the query shape *contains* the cell then we can conveniently short-circuit the depth traversal and
    // grab all documents assigned to this cell/term.  For an intersection of the cell and query shape, we either
    // recursively step down another grid level or we decide heuristically (via prefixGridScanLevel) that there aren't
    // that many points, and so we scan through all terms within this cell (i.e. the term starts with the cell's term),
    // seeing which ones are within the query shape.
    while(!cells.isEmpty() && term != null) {
      final SpatialPrefixGrid.Cell cell = cells.removeFirst();
      assert cell.getLevel() > 0;
      final BytesRef cellTerm = new BytesRef(cell.getBytes());

      //TODO: benchmark this dubious optimization
      //Optimization: If term is "ahead" of this cell (not within and comes after) then short-circuit. This avoids
      // calling potentially expensive queryShape.intersect(cell.getShape()).
      if (!term.startsWith(cellTerm) && cellTerm.compareTo(term) < 0)
        continue;

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

        //Decide whether to continue to divide & conquer, or whether it's time to scan through terms beneath this cell.
        boolean scan = cell.getLevel() >= prefixGridScanLevel;//simple heuristic

        if (!scan) {
          //Divide & conquer
          cells.addAll(0, cell.getSubCells());//add to beginning
        } else {
          //Scan through all terms within this cell to see if they are within the queryShape.
          for(; term != null && term.startsWith(cellTerm); term = termsEnum.next()) {
            //TODO following check is to avoid needless term.utf8ToString().
            if (term.length < grid.getMaxLevels())//intermediate ngram
              continue;
            // We use a simple & fast point instead of grid.getCell(term).getShape()  (a bbox)
            Point p = grid.getPoint(term.utf8ToString());
            if(queryShape.intersect(p, grid.getShapeIO()) == IntersectCase.OUTSIDE)
              continue;

            docsEnum = termsEnum.docs(delDocs, docsEnum);
            addDocs(docsEnum,bits);
          }
        }
      }
    }//cell loop

    return bits;
  }

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
    if (prefixGridScanLevel != that.prefixGridScanLevel) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fieldName != null ? fieldName.hashCode() : 0;
    result = 31 * result + (queryShape != null ? queryShape.hashCode() : 0);
    return result;
  }
}
