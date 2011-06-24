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
  private final int detailLevel;

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

    //cells is treated like a stack. LinkedList conveniently has bulk add to beginning. It's in sorted order so that we
    //  always advance forward through the termsEnum index.
    LinkedList<SpatialPrefixGrid.Cell> cells = new LinkedList<SpatialPrefixGrid.Cell>(
        grid.getWorldCell().getSubCells(queryShape) );

    //This is a recursive algorithm that starts with one or more "big" cells, and then recursively dives down into the
    // first such cell that intersects with the query shape.  It's a depth first traversal because we don't move onto
    // the next big cell (breadth) until we're completely done considering all smaller cells beneath it. For a given
    // cell, if its *within* the query shape then we can conveniently short-circuit the depth traversal and
    // grab all documents assigned to this cell/term.  For an intersection of the cell and query shape, we either
    // recursively step down another grid level or we decide heuristically (via prefixGridScanLevel) that there aren't
    // that many points, and so we scan through all terms within this cell (i.e. the term starts with the cell's term),
    // seeing which ones are within the query shape.
    while(!cells.isEmpty()) {
      final SpatialPrefixGrid.Cell cell = cells.removeFirst();
      IntersectCase intersection = cell.getShapeRel();
      assert intersection != null;
      if (intersection == IntersectCase.OUTSIDE)
        continue;
      final BytesRef cellTerm = new BytesRef(cell.getTokenBytes());
      TermsEnum.SeekStatus seekStat = termsEnum.seek(cellTerm);
      if (seekStat == TermsEnum.SeekStatus.END)
        break;
      if (seekStat == TermsEnum.SeekStatus.NOT_FOUND)
        continue;
      if (intersection == IntersectCase.WITHIN || cell.getLevel() == detailLevel) {
        docsEnum = termsEnum.docs(delDocs, docsEnum);
        addDocs(docsEnum,bits);
      } else {//any other intersection

        //Decide whether to continue to divide & conquer, or whether it's time to scan through terms beneath this cell.
        // Scanning is a performance optimization trade-off.
        boolean scan = cell.getLevel() >= prefixGridScanLevel;//simple heuristic

        if (!scan) {
          //Divide & conquer
          cells.addAll(0, cell.getSubCells(queryShape));//add to beginning
        } else {
          //Scan through all terms within this cell to see if they are within the queryShape. No seek()s.
          for(BytesRef term = termsEnum.term(); term != null && term.startsWith(cellTerm); term = termsEnum.next()) {
            int termLevel = term_getLevel(term);
            if (termLevel > detailLevel)
              continue;
            if (termLevel == detailLevel || term_isLeaf(term)) {
              //TODO should put more thought into implications of box vs point; this is a detail wart
              final String token = term.utf8ToString();
              Shape cShape = termLevel == grid.getMaxLevels() ? grid.getPoint(token) : grid.getCell(token).getShape();
              if(queryShape.intersect(cShape, grid.getShapeIO()) == IntersectCase.OUTSIDE)
                continue;

              docsEnum = termsEnum.docs(delDocs, docsEnum);
              addDocs(docsEnum,bits);
            }
          }//term loop
        }
      }
    }//cell loop

    return bits;
  }

  /* TODO Temporary methods that should migrate to SpatialPrefixGrid.
   * The implementation will need to change when indexing shapes is supported.
   * Perhaps we make some sort of mutable Cell like grid.makeBlankCell() and we add a reset(term) method?
   */
  private int term_getLevel(BytesRef term) {
    return term.length;
  }
  private boolean term_isLeaf(BytesRef term) {
    return term.length == grid.getMaxLevels();
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

    if (!fieldName.equals(that.fieldName)) return false;
    //note that we don't need to look at grid since for the same field it should be the same
    if (prefixGridScanLevel != that.prefixGridScanLevel) return false;
    if (detailLevel != that.detailLevel) return false;
    if (!queryShape.equals(that.queryShape)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fieldName.hashCode();
    result = 31 * result + queryShape.hashCode();
    result = 31 * result + detailLevel;
    return result;
  }
}
