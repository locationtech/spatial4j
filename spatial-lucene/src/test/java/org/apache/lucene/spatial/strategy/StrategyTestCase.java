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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.junit.Before;


public abstract class StrategyTestCase<T extends SpatialFieldInfo> extends SpatialTestCase {

  protected final SpatialArgsParser argsParser = new SpatialArgsParser();
  protected ShapeIO shapeIO;
  protected SpatialStrategy<T> strategy;
  protected T fieldInfo;

  /**
   * This needs to initialize the shapeIO,strategy and fieldInfo
   */
  protected abstract void initStrategy();

  protected abstract Iterator<SampleData> getTestData() throws IOException;


  @Override
  @Before
  public void setUp() throws Exception {
    initStrategy();
    super.setUp();
  }

  @Override
  protected List<Document> getDocuments() throws IOException {
    ArrayList<Document> docs = new ArrayList<Document>();
    Iterator<SampleData> iter = getTestData();
    while( iter.hasNext() ) {
      SampleData data = iter.next();
      Document doc = new Document();
      doc.add( new Field( "id", data.id, Store.YES, Index.ANALYZED ) );
      doc.add( new Field( "name", data.name, Store.YES, Index.ANALYZED ) );
      Shape shape = shapeIO.readShape( data.shape );
      for( Fieldable f : strategy.createFields(fieldInfo, shape, true, true) ) {
        if( f != null ) {
          doc.add( f );
        }
      }
      docs.add( doc );
    }
    return docs;
  }

  protected SearchResults execute( SpatialArgs args, int numDocs ) {
    return executeQuery(strategy.makeQuery(args, fieldInfo ), numDocs );
  }

  public void runTestQueries( Iterator<SpatialTestQuery> queries ) {
    while( queries.hasNext() ) {
      SpatialTestQuery q = queries.next();

      String msg = "Query: "+q.args.toString(shapeIO);
      SearchResults got = execute(q.args, 100 );
      if( q.orderIsImportant ) {
        Iterator<String> ids = q.ids.iterator();
        for( SearchResult r : got.results ) {
          String id = r.document.get( "id" );
          Assert.assertEquals(msg, ids.next(), id );
        }
        if( ids.hasNext() ) {
          Assert.fail( msg + " :: expect more results then we got: "+ids.next() );
        }
      }
      else {
        Collections.sort( q.ids );
        List<String> found = new ArrayList<String>();
        for( SearchResult r : got.results ) {
          found.add( r.document.get( "id" ) );
        }
        Collections.sort( found );
        Assert.assertEquals(msg, q.ids.toString(), found.toString() );
      }
    }
  }
}
