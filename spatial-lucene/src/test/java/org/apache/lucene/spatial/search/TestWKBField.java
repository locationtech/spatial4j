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

package org.apache.lucene.spatial.search;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.base.SpatialOperation;
import org.apache.lucene.spatial.base.jts.JtsGeometry;
import org.apache.lucene.spatial.search.wkb.GeometryTestFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;



/**
 */
public class TestWKBField extends TestCase
{
  public void testGeometryOpertaions() throws Exception
  {
    GeometryFactory factory = new GeometryFactory();

    // 3x3 square at X=-10
    Geometry gA = factory.toGeometry( new Envelope( -10, -7, 0, 3 ) );

    // 2x2 square at X=8
    Geometry gB = factory.toGeometry( new Envelope( 8, 10, 0, 2 ) );

    // 4x4 square at X=-9
    Geometry gC = factory.toGeometry( new Envelope( -9, -5, 0, 4 ) );

    // 1x1 square at X=-9
    Geometry gD = factory.toGeometry( new Envelope( -9, -8, 1, 2 ) );


    SpatialArgs args = new SpatialArgs( SpatialOperation.Intersects );
    args.shape = new JtsGeometry( gA );

    // A does not intersect B
    assertFalse( GeometryTestFactory.get( SpatialOperation.Intersects, gA ).matches( gB ) );

    // A disjoint to B
    assertTrue( GeometryTestFactory.get( SpatialOperation.IsDisjointTo, gA ).matches( gB ) );

    // A intersect C and vis-versa
    assertTrue( GeometryTestFactory.get( SpatialOperation.Intersects, gA ).matches( gC ) );
    assertTrue( GeometryTestFactory.get( SpatialOperation.Intersects, gC ).matches( gA ) );

    // D within A / A contains D
    assertTrue( GeometryTestFactory.get( SpatialOperation.IsWithin, gA ).matches( gD ) );
    assertTrue( GeometryTestFactory.get( SpatialOperation.Contains, gD ).matches( gA ) );
  }
}
