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

package org.apache.lucene.spatial.strategy;


import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public abstract class StrategyTestCase<T extends SpatialFieldInfo> extends SpatialTestCase {

  public static final String DATA_US_STATES = "us-states.txt"; // needs JTS
  
  public static final String QTEST_US_IsWithin_BBox   = "test-us-IsWithin-BBox.txt";
  public static final String QTEST_US_Intersects_BBox = "test-us-Intersects-BBox.txt";
  
  
  protected final SpatialArgsParser argsParser = new SpatialArgsParser();

  protected void executeQueries(
      SpatialStrategy<T> strategy,
      ShapeIO shapeIO,
      T fieldInfo,
      String testDataFile,
      String ... testQueryFile ) throws IOException {
    List<Document> testDocuments = getDocuments(testDataFile, shapeIO, strategy, fieldInfo);
    addDocuments(testDocuments);
    verifyDocumentsIndexed(testDocuments.size());
    
    for( String path : testQueryFile ) {
      Iterator<SpatialTestQuery> testQueryIterator = getTestQueries(path, shapeIO);
      runTestQueries(testQueryIterator, shapeIO, strategy, fieldInfo);
    }
  }

  protected List<Document> getDocuments(String testDataFile, ShapeIO shapeIO, SpatialStrategy<T> strategy, T fieldInfo) throws IOException {
    Iterator<SampleData> sampleData = getSampleData(testDataFile);
    List<Document> documents = new ArrayList<Document>();
    while (sampleData.hasNext()) {
      SampleData data = sampleData.next();
      Document document = new Document();
      document.add(new Field("id", data.id, Store.YES, Index.ANALYZED));
      document.add(new Field("name", data.name, Store.YES, Index.ANALYZED));
      Shape shape = shapeIO.readShape(data.shape);
      for (Fieldable f : strategy.createFields(fieldInfo, shape, true, true)) {
        document.add(f);
      }
      documents.add(document);
    }
    return documents;
  }

  protected Iterator<SampleData> getSampleData(String testDataFile) throws IOException {
    //File file = new File(getClass().getClassLoader().getResource(testDataFile).getFile());
    // ugg maven class loading still not working for me (ryan)
    File file = new File( "../spatial-data/src/main/resources/"+testDataFile );
    return new SampleDataReader(file);
  }

  protected Iterator<SpatialTestQuery> getTestQueries(String testQueryFile, ShapeIO shapeIO) throws IOException {
    // ugg maven class loading still not working for me (ryan)
//    new File(getClass().getClassLoader().getResource(testQueryFile).getFile()
    File file = new File( "src/test/resources/"+testQueryFile );
    return SpatialTestQuery.getTestQueries(
        argsParser, shapeIO, file);
  }

  public void runTestQueries(Iterator<SpatialTestQuery> queries, ShapeIO shapeIO, SpatialStrategy<T> strategy, T fieldInfo) {
    while (queries.hasNext()) {
      SpatialTestQuery q = queries.next();

      String msg = "Query: " + q.args.toString(shapeIO);
      SearchResults got = executeQuery(strategy.makeQuery(q.args, fieldInfo), 100);
      if (q.orderIsImportant) {
        Iterator<String> ids = q.ids.iterator();
        for (SearchResult r : got.results) {
          String id = r.document.get("id");
          Assert.assertEquals(msg, ids.next(), id);
        }
        if (ids.hasNext()) {
          Assert.fail(msg + " :: expect more results then we got: " + ids.next());
        }
      } else {
        Collections.sort(q.ids);
        List<String> found = new ArrayList<String>();
        for (SearchResult r : got.results) {
          found.add(r.document.get("id"));
        }
        Collections.sort(found);
        Assert.assertEquals(msg, q.ids.toString(), found.toString());
      }
    }
  }
}
