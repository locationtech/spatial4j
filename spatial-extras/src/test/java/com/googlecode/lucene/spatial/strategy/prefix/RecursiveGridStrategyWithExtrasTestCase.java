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

package com.googlecode.lucene.spatial.strategy.prefix;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;

import org.apache.lucene.spatial.base.prefix.geohash.GeohashPrefixTree;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.SpatialMatchConcern;
import org.apache.lucene.spatial.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;


public class RecursiveGridStrategyWithExtrasTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    int maxLength = GeohashPrefixTree.getMaxLevelsPossible();
    this.ctx = new JtsSpatialContext();
    GeohashPrefixTree grid = new GeohashPrefixTree(
        ctx, maxLength );
    this.strategy = new RecursivePrefixTreeStrategy( grid );
    //((RecursiveGridStrategy)strategy).setDistErrPct(0.1);//little faster
    this.fieldInfo = new SimpleSpatialFieldInfo( "geohash" );
    this.storeShape = false;//unnecessary
  }

  /**
   * For now, the only difference from the Simple version is that this uses JtsSpatialContext
   */
  @Test
  public void testWorldCitiesWithinBox() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_Cities_IsWithin_BBox);
  }

  @Test
  public void testPolygonIndex() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_STATES_POLY);
    executeQueries(SpatialMatchConcern.FILTER, QTEST_States_Intersects_BBox);
  }
}
