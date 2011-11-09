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

package com.googlecode.lucene.spatial.strategy.vector;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesFieldInfo;
import org.apache.lucene.spatial.strategy.vector.TwoDoublesStrategy;
import org.apache.lucene.spatial.strategy.util.TrieFieldInfo;
import org.apache.lucene.spatial.SpatialMatchConcern;
import org.apache.lucene.spatial.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class TwoDoublesStrategyTestCase extends StrategyTestCase<TwoDoublesFieldInfo> {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.ctx = new JtsSpatialContext();
    this.strategy = new TwoDoublesStrategy( new SimpleSpatialContext(),//TODO bug? why not ctx?
      new TrieFieldInfo(), FieldCache.NUMERIC_UTILS_DOUBLE_PARSER );
    this.fieldInfo = new TwoDoublesFieldInfo( "vector2d" );
  }

  @Test
  public void testPointStrategyWithJts() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }
}
