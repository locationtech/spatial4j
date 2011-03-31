package org.apache.lucene.spatial.strategy.prefix;

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

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoringRewrite;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.PerReaderTermState;

/**
 * @see AutomatonQuery, WildcardQuery
 */
public class SpatialPrefixGridQuery extends WildcardQuery {

  /**
   * Constructs a query for terms matching <code>term</code>.
   */
  public SpatialPrefixGridQuery(Term term, int bestResolution, PrefixGridSimilarity gridSimilarity) {
    super(term);
    setRewriteMethod(new PrefixGridTermQueryRewrite(bestResolution, gridSimilarity));
  }

  // ================================================= Inner Classes =================================================

  private class PrefixGridTermQueryRewrite extends ScoringRewrite<BooleanQuery> {

    private int bestResolution;
    private PrefixGridSimilarity gridSimilarity;

    private PrefixGridTermQueryRewrite(int bestResolution, PrefixGridSimilarity gridSimilarity) {
      this.bestResolution = bestResolution;
      this.gridSimilarity = gridSimilarity;
    }

    @Override
    protected void checkMaxClauseCount(int count) throws IOException {
      if (count > BooleanQuery.getMaxClauseCount()) {
        throw new BooleanQuery.TooManyClauses();
      }
    }

    @Override
    protected BooleanQuery getTopLevelQuery() throws IOException {
      return new BooleanQuery(true);
    }

    @Override
    protected void addClause(
        BooleanQuery booleanQuery,
        Term term,
        int docCount,
        float boost,
        PerReaderTermState perReaderTermState) throws IOException {
      TermQuery termQuery = new TermQuery(term, perReaderTermState);
      termQuery.setBoost(boost);
      PrefixGridTermQuery gridTermQuery = new PrefixGridTermQuery(termQuery, bestResolution, gridSimilarity);
      booleanQuery.add(gridTermQuery, BooleanClause.Occur.SHOULD);
    }
  }
}
