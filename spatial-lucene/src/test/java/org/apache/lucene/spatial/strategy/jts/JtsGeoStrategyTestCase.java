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

package org.apache.lucene.spatial.strategy.jts;


import java.io.IOException;

import org.apache.lucene.index.codecs.CodecProvider;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.junit.Test;


public class JtsGeoStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    assumeFalse("preflex format only supports UTF-8 encoded bytes",
        "PreFlex".equals(CodecProvider.getDefault().getDefaultFieldCodec()) );
  }

  @Test
  public void testJtsGeoStrategy() throws IOException {
    JtsShapeIO shapeIO = new JtsShapeIO();
    JtsGeoStrategy strategy = new JtsGeoStrategy(shapeIO.factory);
    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geo" );

    if( true ) {
    executeQueries( strategy, shapeIO, finfo,
        DATA_STATES_POLY,
        QTEST_States_Intersects_BBox,
        QTEST_States_IsWithin_BBox );
    }

    // Can not close, there are still open files
    if( false ) {
    executeQueries( strategy, shapeIO, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
    }
  }
}
