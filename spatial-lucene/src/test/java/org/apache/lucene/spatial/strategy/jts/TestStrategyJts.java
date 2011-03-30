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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialTestCase;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;


public class TestStrategyJts extends SpatialTestCase {

  JtsShapeIO shapeIO;
  JtsGeoStrategy strategy;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    shapeIO = new JtsShapeIO( new GeometryFactory() );
    strategy = new JtsGeoStrategy( shapeIO.factory );
  }
  
  @Override
  protected List<Document> getDocuments() throws IOException {

    File file = new File( "../data/us-states.txt" );
    System.out.println( file.getAbsolutePath() );
    
    ArrayList<Document> docs = new ArrayList<Document>();
    SampleDataReader reader = new SampleDataReader(file);
    while( reader.hasNext() ) {
      SampleData data = reader.next();
      Document doc = new Document();
      doc.add( new Field( "name", data.name, Store.YES, Index.ANALYZED ) );
      doc.add( new Field( "id", "state-"+data.fips, Store.YES, Index.ANALYZED ) );
      
      Shape shape = shapeIO.readShape( data.shape );
      SimpleSpatialFieldInfo info = new SimpleSpatialFieldInfo( "shape" );
      
      for( Fieldable f : strategy.createFields(info, shape, true, true) ) {
        doc.add( f );
      }
      docs.add( doc );
    }
    return docs;
  }

  @Test
  public void testSearch() throws IOException {
    System.out.println( "running simple query..." );
    
    SearchResults got = executeQuery(new MatchAllDocsQuery(), 5 );
    
    System.out.println( "got: "+got.numFound );
    
  }
}
