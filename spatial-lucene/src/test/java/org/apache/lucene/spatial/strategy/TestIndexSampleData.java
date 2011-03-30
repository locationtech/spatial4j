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


import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.lucene.spatial.base.io.geonames.Geoname;
import org.apache.lucene.spatial.base.io.geonames.GeonamesReader;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.junit.Test;


/**
 * This is just a quick idea for *simple* tests
 */
public class TestIndexSampleData {


  @Test
  public void testSampleData() throws IOException {
    // This data is only supported by JTS
    JtsShapeIO io = new JtsShapeIO();
    
    File file = new File( "../data/countries.txt" );
    System.out.println( file.getAbsolutePath() );
    
    SampleDataReader reader = new SampleDataReader(file);
    while( reader.hasNext() ) {
      SampleData data = reader.next();
      Shape shape = io.readShape( data.shape );
    }
    Assert.assertEquals( 248, reader.getCount() );
    
    
    file = new File( "../data/us-states.txt" );
    System.out.println( file.getAbsolutePath() );
    
    reader = new SampleDataReader(file);
    while( reader.hasNext() ) {
      SampleData data = reader.next();
      Shape shape = io.readShape( data.shape );
    }
    Assert.assertEquals( 51, reader.getCount() ); // 50 + DC
  }
  

  @Test
  public void testIndexGeonames() throws IOException {
    // This could use JTS or Simple
    JtsShapeIO io = new JtsShapeIO();
    
    File file = new File( "../data/geonames-IE.txt" );
    System.out.println( file.getAbsolutePath() );
    
    GeonamesReader reader = new GeonamesReader(file);
    while( reader.hasNext() ) {
      Geoname geoname = reader.next();
      Shape shape = io.makePoint( geoname.longitude, geoname.latitude );
      // TODO... index it...
    }
    Assert.assertEquals( 22929, reader.getCount() );
  }
  
}
