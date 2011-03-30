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

package org.apache.lucene.spatial.strategy.external;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialTestQuery;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;


public class ExternalIndexStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Override
  protected void initStrategy() {
    shapeIO = new JtsShapeIO( new GeometryFactory() );

    strategy = new ExternalIndexStrategy( shapeIO );
    fieldInfo = new SimpleSpatialFieldInfo( "geo" );
  }

  @Override
  protected Iterator<SampleData> getTestData() throws IOException {
//    File file = new File(getClass().getClassLoader()
//        .getResource("us-states.txt").getFile());
    File file = new File("../spatial-data/src/main/resources/us-states.txt");
    return new SampleDataReader(file);
  }

  @Test
  public void testSpatialSearch() throws IOException {
    System.out.println( "running simple query..." );

    SearchResults got = executeQuery(new MatchAllDocsQuery(), 5 );
    Assert.assertEquals( 51, got.numFound );

    // Test Contains
    File file = new File("src/test/resources/test-us-Intersects-BBox.txt");
    System.out.println( file.getAbsolutePath() );
    runTestQueries( SpatialTestQuery.getTestQueries(argsParser, shapeIO, file) );
  }
}
