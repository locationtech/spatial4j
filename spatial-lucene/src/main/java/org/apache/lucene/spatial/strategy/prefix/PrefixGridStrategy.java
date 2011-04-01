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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;

public class PrefixGridStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  protected final SpatialPrefixGrid grid;
  protected final int maxLength;
  private PrefixGridSimilarity prefixGridSimilarity = new PrefixGridSimilarity.SimplePrefixGridSimilarity();

  public PrefixGridStrategy(SpatialPrefixGrid grid, int maxLength) {
    this.grid = grid;
    this.maxLength = maxLength;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo indexInfo, Shape shape, boolean index, boolean store) {
    List<String> match = simplifyGridCells(grid.readCells(shape));
    BasicGridFieldable f = new BasicGridFieldable(indexInfo.getFieldName(), store);
    f.tokens = buildBasicTokenStream(match);

    if (store) {
      f.value = match.toString(); //reader.toString( shape );
    }
    return f;
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo info) {
    throw new UnsupportedOperationException("not implemented yet...");
  }

  @Override
  public Filter makeFilter(SpatialArgs args, SimpleSpatialFieldInfo field) {
    return new QueryWrapperFilter( makeQuery(args, field) );
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo field) {
    if (args.getOperation() != SpatialOperation.Intersects &&
        args.getOperation() != SpatialOperation.IsWithin &&
        args.getOperation() != SpatialOperation.Overlaps &&
        args.getOperation() != SpatialOperation.SimilarTo) {
      // TODO -- can translate these other query types
      throw new UnsupportedOperationException("Unsupported Operation: " + args.getOperation());
    }

    // TODO... resolution should help scoring...
    int resolution = grid.getBestLevel(args.getShape());
    List<String> match = simplifyGridCells(grid.readCells(args.getShape()));

    // TODO -- could this all happen in one pass?
    BooleanQuery query = new BooleanQuery(true);

    if (args.getOperation() == SpatialOperation.IsWithin) {
      for (String token : match) {
        Term term = new Term(field.getFieldName(), token + "*");
        SpatialPrefixGridQuery q = new SpatialPrefixGridQuery(term, resolution, prefixGridSimilarity);
        query.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
      }
    } else {
      // Need to add all the parent queries
      Set<String> parents = new TreeSet<String>();
      for (String token : match) {
        for (int i = 1; i < token.length(); i++) {
          parents.add(token.substring(0, i));
        }

        Term term = new Term(field.getFieldName(), token + "*");
        SpatialPrefixGridQuery q = new SpatialPrefixGridQuery(term, resolution, prefixGridSimilarity);
        query.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
      }

      for (String t : parents) {
        Term term = new Term(field.getFieldName(), t);
        Query q = new PrefixGridTermQuery(new TermQuery(term), resolution, prefixGridSimilarity);
        query.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
      }
    }
    return query;
  }

  // ================================================= Helper Methods ================================================

  protected List<String> simplifyGridCells(List<String> gridCells) {
    List<String> newGridCells = new ArrayList<String>();
    for (String gridCell : gridCells) {
      newGridCells.add(gridCell.toLowerCase(Locale.ENGLISH).substring(0, gridCell.length() - 1));
    }
    return newGridCells;
  }

  protected TokenStream buildBasicTokenStream(List<String> gridCells) {
    if (maxLength > 0) {
      return new RemoveDuplicatesTokenFilter(
          new TruncateFilter(new StringListTokenizer(gridCells), maxLength));
    } else {
      return new StringListTokenizer(gridCells);
    }
  }
}
