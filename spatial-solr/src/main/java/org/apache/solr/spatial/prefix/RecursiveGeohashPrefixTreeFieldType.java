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

package org.apache.solr.spatial.prefix;

import org.apache.lucene.spatial.base.prefix.geohash.GeohashPrefixTree;
import org.apache.lucene.spatial.strategy.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.strategy.prefix.PrefixTreeStrategy;


/**
 *
 */
public class RecursiveGeohashPrefixTreeFieldType extends PrefixTreeFieldType {

  @Override
  protected PrefixTreeStrategy initStrategy(Integer maxLevels, Double degrees) {
    GeohashPrefixTree grid;
    if (maxLevels != null) {
      grid = new GeohashPrefixTree(ctx,maxLevels);
    } else {
      grid = new GeohashPrefixTree(ctx,GeohashPrefixTree.getMaxLevelsPossible());
      int level = grid.getLevelForDistance(degrees) + 1;//returns 1 greater
      if (level != grid.getMaxLevels())
        grid = new GeohashPrefixTree(ctx,level);
    }
    return new RecursivePrefixTreeStrategy(grid);
  }
}

