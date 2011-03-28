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

package org.apache.lucene.spatial.search.prefix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

public class PrefixGridQueryBuilder implements SpatialQueryBuilder<SimpleSpatialFieldInfo> {
  
  private final SpatialPrefixGrid grid;

  public PrefixGridQueryBuilder(SpatialPrefixGrid grid) {
    this.grid = grid;
  }

  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo info) {
    throw new UnsupportedOperationException( "not implemented yet..." );
  }

  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo queryInfo) {
    if (args.getOperation() != SpatialOperation.Intersects &&
        args.getOperation() != SpatialOperation.IsWithin &&
        args.getOperation() != SpatialOperation.Overlaps &&
        args.getOperation() != SpatialOperation.SimilarTo) {
      // TODO -- can translate these other query types
      throw new UnsupportedOperationException("Unsupported Operation: " + args.getOperation());
    }

    // TODO... resolution should help scoring...
    int resolution = grid.getBestLevel(args.getShape());
    List<CharSequence> match = grid.readCells(args.getShape());

    // TODO -- could this all happen in one pass?
    BooleanQuery query = new BooleanQuery(true);

    if (args.getOperation() == SpatialOperation.IsWithin) {
      for (CharSequence token : match) {
        Term term = new Term(queryInfo.getFieldName(), token.toString());
        SpatialPrefixGridQuery q = new SpatialPrefixGridQuery(term);
        query.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
      }
    } else {
      // Need to add all the parent queries
      Set<String> terms = new HashSet<String>();
      Set<String> parents = new HashSet<String>();
      for (CharSequence token : match) {
        for (int i = 1; i < token.length(); i++) {
          parents.add(token.subSequence(0, i) + "*");
        }
        terms.add(token.toString().replace('+', '*'));

        Term term = new Term(queryInfo.getFieldName(), token.toString());
        SpatialPrefixGridQuery q = new SpatialPrefixGridQuery(term);
        query.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
      }

      // These all include the '*'
      List<String> sorted = new ArrayList<String>(parents);
      Collections.sort(sorted);
      for (String t : sorted) {
        if (!terms.contains(t)) {
          Term term = new Term(queryInfo.getFieldName(), t);
          PrefixQuery q = new PrefixQuery(term);
          query.add( new BooleanClause(q, BooleanClause.Occur.SHOULD));
        }
      }
    }
    return query;
  }
}
