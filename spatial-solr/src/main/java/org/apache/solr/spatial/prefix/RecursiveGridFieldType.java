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

import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.prefix.RecursiveGridStrategy;
import org.apache.lucene.spatial.strategy.prefix.PrefixGridStrategy;


/**
 *
 */
public class RecursiveGridFieldType extends PrefixGridFieldType {

  @Override
  protected PrefixGridStrategy initStrategy(Integer maxLevels, Double degrees) {
    GeohashSpatialPrefixGrid grid;
    if (maxLevels != null) {
      grid = new GeohashSpatialPrefixGrid(reader,maxLevels);
    } else {
      grid = new GeohashSpatialPrefixGrid(reader,GeohashSpatialPrefixGrid.getMaxLevelsPossible());
      int level = grid.getLevelForDistance(degrees) + 1;//returns 1 greater
      if (level != grid.getMaxLevels())
        grid = new GeohashSpatialPrefixGrid(reader,level);
    }
    return new RecursiveGridStrategy(grid);
  }

}

